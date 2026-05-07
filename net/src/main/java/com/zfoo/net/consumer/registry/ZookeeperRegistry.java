/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.net.consumer.registry;

import com.zfoo.event.manager.EventBus;
import com.zfoo.net.NetContext;
import com.zfoo.net.consumer.event.ConsumerStartEvent;
import com.zfoo.net.consumer.event.ProviderStartEvent;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.tcp.TcpClient;
import com.zfoo.net.core.tcp.TcpServer;
import com.zfoo.net.session.Session;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.collection.concurrent.ConcurrentArrayList;
import com.zfoo.protocol.collection.concurrent.ConcurrentHashSet;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.util.*;
import com.zfoo.scheduler.manager.SchedulerBus;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service registration and discovery via ZooKeeper.
 *
 * @author godotg
 */
public class ZookeeperRegistry implements IRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private static final long RETRY_SECONDS = 5;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(new ConfigThreadFactory());

    private static class ConfigThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        // config-p1-t1 = config-pool-1-thread-1
        ConfigThreadFactory() {
            this.group = Thread.currentThread().getThreadGroup();
            namePrefix = "config-p" + poolNumber.getAndIncrement() + "-t";
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new FastThreadLocalThread(group, runnable, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            t.setUncaughtExceptionHandler((thread, e) -> logger.error(thread.toString(), e));
            return t;
        }
    }


    private CuratorFramework curator;
    /**
     * Curator cache for watching provider nodes.
     */
    private CuratorCache providerCuratorCache;
    /**
     * The set of providers that the local consumer needs to connect to.
     */
    private final Set<Register> providerRegisterSet = new ConcurrentHashSet<>();
    /**
     * All CuratorCache objects added via addListener(); does not include providerCuratorCache.
     */
    private final List<CuratorCache> listenerList = new ConcurrentArrayList<>();

    /**
     * This method can be summarized in 3 steps:
     * 1. If the local node is configured as a service provider, start it (not yet registered in ZK).
     * 2. Start the Curator framework: register this node in ZK if it is a provider; connect to relevant providers if it is a consumer.
     * 3. Watch for changes to provider nodes (since this node acts as a consumer, provider changes affect it).
     * <p>
     * Summary: the registry module ultimately affects:
     * - ClientSessionMap: sessions established by this consumer when connecting to providers.
     * - ServerSessionMap: sessions of clients that connect to this node as a server.
     */

    private String rootPath = "/zfoo";
    private String providerRootPath = "/zfoo/provider";
    private String consumerRootPath = "/zfoo/consumer";

    @Override
    public void start() {
        var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();
        if (Objects.isNull(registryConfig)) {
            logger.info("Stand alone startup of singleton");
            return;
        }

        if (StringUtils.isNotBlank(registryConfig.getPath())) {
            rootPath = StringUtils.trim(registryConfig.getPath());
            providerRootPath = rootPath + "/provider";
            consumerRootPath = rootPath + "/consumer";
        }

        // Start the local service provider first (a provider is simply a TcpServer)
        startProvider();

        // Start the Curator framework: 1. register this node as a provider in ZK; 2. connect to relevant providers as a consumer
        startCurator();

        // Watch for provider node additions or removals, as they affect this node acting as a consumer
        startProviderCache();
    }

    private void startProvider() {
        // ProviderConfig contains the provider information for this process
        var providerConfig = NetContext.getConfigManager().getLocalConfig().getProvider();

        // Registry (ZK) is configured but no local service provider is declared
        if (Objects.isNull(providerConfig)) {
            logger.info("Distributed startup with no providers");
            return;
        }

        // A service provider is simply a TcpServer; no specific port is mandated — an available port is used
        var providerHostAndPort = providerConfig.localHostAndPortOrDefault();
        var providerServer = new TcpServer(providerHostAndPort);
        providerServer.start();
        EventBus.post(ProviderStartEvent.valueOf(providerHostAndPort));
    }

    /**
     * Start the Curator framework. On successful connection, ensure the three persistent ZK nodes
     * (/zfoo, /zfoo/provider, /zfoo/consumer) exist.
     */
    private void startCurator() {
        var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();

        if (!registryConfig.getCenter().toLowerCase().matches("zookeeper")) {
            throw new IllegalArgumentException(StringUtils
                    .format("[center:{}] only zookeeper is supported as the registry center", JsonUtils.object2String(registryConfig)));
        }

        // Read ZK configuration and connect to the ZooKeeper server
        var zookeeperConnectStr = HostAndPort.toHostAndPortListStr(HostAndPort.toHostAndPortList(registryConfig.getAddress().values()));
        var builder = CuratorFrameworkFactory.builder();
        builder.connectString(zookeeperConnectStr);
        if (registryConfig.hasZookeeperAuthor()) {
            builder.authorization("digest", StringUtils.bytes(registryConfig.toZookeeperAuthor()));
        }
        builder.sessionTimeoutMs(40_000);
        builder.connectionTimeoutMs(10_000);
        builder.retryPolicy(new RetryNTimes(1, 3_000));

        curator = builder.build();
        curator.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState state) {
                switch (state) {
                    // ZK client lost connection to the server (use local cached config)
                    case LOST:
                        logger.error("[zookeeper:{}] lost connection, cache used", zookeeperConnectStr);
                        break;

                    // SUSPENDED and READ_ONLY states are not monitored
                    case SUSPENDED:
                    case READ_ONLY:
                        logger.warn("[zookeeper:{}] ignored [state{}]", zookeeperConnectStr, state);
                        break;

                    // ZK client reconnected to the server
                    case CONNECTED:
                    case RECONNECTED:
                        // Ensure the three persistent root nodes exist; create them if missing
                        createZookeeperRootPath();
                        break;

                    default:
                        logger.error("[zookeeper:{}] unknown [state{}]", zookeeperConnectStr, state);
                }
            }
        }, executor);

        curator.start();
        try {
            curator.blockUntilConnected();
        } catch (Throwable t) {
            throw new RuntimeException("Start zookeeper exception", t);
        }
    }

    /**
     * Check whether the three persistent ZK nodes (/zfoo, /zfoo/provider, /zfoo/consumer) exist;
     * create them if they are missing.
     */
    private void createZookeeperRootPath() {
        executor.execute(() -> {
            try {
                // Check and optionally create the ZooKeeper root path
                var rootStat = curator.checkExists().forPath(rootPath);
                // Root node does not exist
                if (Objects.isNull(rootStat)) {
                    var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();
                    var builder = curator.create();
                    builder.creatingParentsIfNeeded();
                            // Verify ZK connection authorisation
                    if (registryConfig.hasZookeeperAuthor()) {
                        var zookeeperAuthorStr = registryConfig.toZookeeperAuthor();
                        var aclList = List.of(new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(zookeeperAuthorStr))));
                        builder.withACL(aclList);
                    }
                    // The root node is a persistent node
                    builder.withMode(CreateMode.PERSISTENT);
                    // Actually create the root node
                    builder.forPath(rootPath, StringUtils.bytes(registryConfig.getCenter()));
                } else {
                    var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();
                    // Read data stored at the root node
                    var bytes = curator.getData().storingStatIn(new Stat()).forPath(rootPath);
                    // Convert root node data from bytes to a string
                    var rootPathData = StringUtils.bytesToString(bytes);

                    // Verify the root node content matches the configured center
                    if (!rootPathData.equals(registryConfig.getCenter())) {
                        throw new RuntimeException(StringUtils.format("zookeeper rootPath[{}] misconfigured [{}], expected [{}], check the relevant nodes and restart", rootPath, rootPathData, registryConfig.getCenter()));
                    }

                    // Verify the ZooKeeper root node ACL permissions
                    if (registryConfig.hasZookeeperAuthor()) {
                        try {
                            var providerRootPathAclList = curator.getACL().forPath(rootPath);
                            AssertionUtils.notEmpty(providerRootPathAclList);
                            AssertionUtils.isTrue(providerRootPathAclList.size() == 1);
                            var zookeeperAuthorStr = registryConfig.toZookeeperAuthor();
                            var aclList = List.of(new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(zookeeperAuthorStr))));
                            AssertionUtils.isTrue(providerRootPathAclList.get(0).equals(aclList.get(0)));
                        } catch (Exception e) {
                            throw new RuntimeException(StringUtils.format("zookeeper rootPath[{}] permissions are misconfigured [{}]", rootPath, ExceptionUtils.getMessage(e)));
                        }
                    }

                }

                // /zfoo/provider — check the provider node; create it if missing
                var providerStat = curator.checkExists().forPath(providerRootPath);
                if (Objects.isNull(providerStat)) {
                    curator.create()
                            .withMode(CreateMode.PERSISTENT)
                            .forPath(providerRootPath, ArrayUtils.EMPTY_BYTE_ARRAY);
                }

                // /zfoo/consumer — check the consumer node; create it if missing
                var consumerStat = curator.checkExists().forPath(consumerRootPath);
                if (Objects.isNull(consumerStat)) {
                    curator.create()
                            .withMode(CreateMode.PERSISTENT)
                            .forPath(consumerRootPath, ArrayUtils.EMPTY_BYTE_ARRAY);
                }

                // Register this node as a provider, or connect to all relevant providers as a consumer
                initZookeeper();
            } catch (Throwable t) {
                logger.error("Zookeeper failed to create zookeeper root path, wait [{}] seconds to recreate", RETRY_SECONDS, t);
                SchedulerBus.schedule(() -> createZookeeperRootPath(), RETRY_SECONDS, TimeUnit.SECONDS);
            }
        });
    }

    private void startProviderCache() {
        // Initialize providerCache
        providerCuratorCache = CuratorCache.builder(curator, providerRootPath)
                .withExceptionHandler(e -> {
                    logger.error("providerCuratorCache unknown exception", e);
                    initZookeeper();
                })
                .build();

        providerCuratorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData oldData, ChildData newData) {
                switch (type) {
                    case NODE_CHANGED:
                        logger.error("No need to deal with [oldData:{}] [newData:{}]", childDataToString(oldData), childDataToString(newData));
                        initZookeeper();
                        break;
                    case NODE_CREATED: // A new provider node may have appeared that this consumer cares about
                        var providerStr = StringUtils.substringAfterFirst(newData.getPath(), providerRootPath + StringUtils.SLASH);
                        var providerRegister = Register.parseString(providerStr);
                        var localRegisterVO = NetContext.getConfigManager().getLocalConfig().toLocalRegister();
                        // If the newly started provider is one this consumer cares about, attempt to connect
                        // This means: if multiple consumers start, they will eventually all connect
                        if (Register.providerHasConsumer(providerRegister, localRegisterVO)) {
                            providerRegisterSet.add(providerRegister);
                            checkConsumer();
                            logger.info("Discover new subscription service of provider [{}]", providerStr);
                        }
                        break;
                    case NODE_DELETED:
                        var oldProviderStr = StringUtils.substringAfterFirst(oldData.getPath(), providerRootPath + StringUtils.SLASH);
                        var oldProvider = Register.parseString(oldProviderStr);
                        if (providerRegisterSet.contains(oldProvider)) {
                            providerRegisterSet.remove(oldProvider);
                            checkConsumer();
                            logger.info("Unsubscribe from the service of provider [{}]", oldProviderStr);
                        }
                        break;
                    default:
                }
            }

            @Override
            public void initialized() {
                SchedulerBus.schedule(() -> initZookeeper(), 3, TimeUnit.SECONDS);
            }
        }, executor);

        providerCuratorCache.start();
    }

    private void initZookeeper() {
        executor.execute(() -> {
            try {
                // If this node is a provider, register it as an ephemeral node in ZK
                initLocalProvider();

                // If this node is a consumer, connect to all relevant providers
                initConsumerCache();
            } catch (Throwable t) {
                logger.error("Zookeeper failed to initialize, wait [{}] seconds to reinitialize", RETRY_SECONDS, t);
                SchedulerBus.schedule(() -> initZookeeper(), RETRY_SECONDS, TimeUnit.SECONDS);
            }
        });
    }

    /**
     * If this node is a service provider, register it in ZooKeeper as an ephemeral node.
     */
    private void initLocalProvider() throws Exception {
        var localRegisterVO = NetContext.getConfigManager().getLocalConfig().toLocalRegister();
        if (Objects.nonNull(localRegisterVO.getProviderConfig())) {
            var localProviderVoStr = localRegisterVO.toProviderString();
            var localProviderPath = providerRootPath + StringUtils.SLASH + localProviderVoStr;

            // /zfoo/provider
            // applicationNameTest | 192.168.1.104:12400 | provider:[myProviderModule-provider1, myProviderModule-provider2]
            var localProviderStat = curator.checkExists().forPath(localProviderPath);
            if (Objects.isNull(localProviderStat)) {
                curator.create()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(localProviderPath, StringUtils.EMPTY.getBytes());
                logger.info("Provider register successful at [{}]", localProviderVoStr);
            } else {
                // The provider node already exists; guard against stale ephemeral nodes left over from a previous session
                var curatorSessionId = curator.getZookeeperClient().getZooKeeper().getSessionId();
                var providerNodeSessionId = localProviderStat.getEphemeralOwner();
                if (curatorSessionId != providerNodeSessionId) {
                    curator.delete()
                            .guaranteed()
                            .deletingChildrenIfNeeded()
                            .withVersion(localProviderStat.getVersion())
                            .forPath(localProviderPath);
                    throw new RuntimeException(StringUtils.format("session of curator[sessionId:{}] and providerNode[sessionId:{}] can not match, delete old old session data"
                            , curatorSessionId, providerNodeSessionId));
                }
            }
        }
    }

    /**
     * Iterate over all provider nodes in ZK to find those this node cares about, then connect to them.
     * Note: acting as a provider only requires starting a TcpServer, which is straightforward.
     * Acting as a consumer requires connecting to all providers that have already registered in ZK.
     */
    private void initConsumerCache() throws Exception {
        // /zfoo/provider/applicationNameTest | 192.168.1.104:12400 | provider:[myProviderModule-provider1, myProviderModule-provider2]
        var localRegisterVO = NetContext.getConfigManager().getLocalConfig().toLocalRegister();
        // Initialize providerCacheSet by iterating over all registered provider nodes
        var remoteProviderSet = curator.getChildren().forPath(providerRootPath).stream()
                .filter(it -> StringUtils.isNotBlank(it) && !"null".equals(it))
                .map(it -> Register.parseString(it))
                .filter(it -> Objects.nonNull(it))
                // Keep only those nodes that this consumer cares about
                .filter(it -> Register.providerHasConsumer(it, localRegisterVO))
                .collect(Collectors.toSet());

        providerRegisterSet.clear();

        // Store the relevant providers; TcpClients will be created to connect to them and sessions saved to ClientSessionMap
        providerRegisterSet.addAll(remoteProviderSet);

        // Initialize consumers; changes to providerCacheSet will update the consumer connections
        // If this node fails to connect to a remote provider, it keeps retrying
        checkConsumer();
    }

    /**
     * Called whenever a provider node is added or removed, since either event may affect this consumer.
     */
    @Override
    public void checkConsumer() {
        if (curator == null) {
            return;
        }

        if (curator.getState() == CuratorFrameworkState.STOPPED) {
            return;
        }

        executor.execute(ThreadUtils.safeRunnable(() -> doCheckConsumer()));
    }

    /**
     * Verify that all providers this consumer needs to connect to are connected.
     * For any provider not yet connected, create a TcpClient to establish the connection.
     */
    private void doCheckConsumer() {
        if (curator.getState() != CuratorFrameworkState.STARTED) {
            logger.error("Curator has not been started yet, ignoring this consumer check");
            return;
        }

        var recheckFlag = false;

        for (var providerCache : providerRegisterSet) {
            // Skip providers that already have an active consumer session
            var consumerClientList = new ArrayList<Session>();
            NetContext.getSessionManager().forEachClientSession(new Consumer<Session>() {
                @Override
                public void accept(Session session) {
                    if (session.getConsumerRegister() != null && session.getConsumerRegister().equals(providerCache)) {
                        consumerClientList.add(session);
                    }
                }
            });

            // consumerClientList size >= 1 means the consumer successfully connected
            if (consumerClientList.size() == 1) {
                var consumer = consumerClientList.get(0);
                if (!SessionUtils.isActive(consumer)) {
                    recheckFlag = true;
                    NetContext.getSessionManager().removeClientSession(consumer);
                    logger.error("[consumer:{}] lost connection, removed from ClientSession", consumer);
                }
                continue;
            }

            if (consumerClientList.size() > 1) {
                logger.error("[consumerClientList:{}] are multiple duplicate [RegisterVO:{}]", consumerClientList, providerCache);
                continue;
            }

            try {
                // Acting as a consumer: create a TcpClient to connect to the service provider
                var client = new TcpClient(HostAndPort.valueOf(providerCache.getProviderConfig().getAddress()));
                var session = client.start();
                if (session == null) {
                    recheckFlag = true;
                    continue;
                }

                session.setConsumerRegister(providerCache);
                logger.info("Consumer starts consuming the provider:[{}]", providerCache);
                EventBus.post(ConsumerStartEvent.valueOf(providerCache, session));
            } catch (Throwable t) {
                logger.error("[consumer:{}] failed to start, wait [{}] seconds to recheck consumer", providerCache, RETRY_SECONDS, t);
                recheckFlag = true;
            }
        }

        // Write this consumer's info to an ephemeral node under /consumer
        updateConsumerData();

        if (recheckFlag) {
            SchedulerBus.schedule(() -> checkConsumer(), RETRY_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void updateConsumerData() {
        var list = new ArrayList<String>();
        NetContext.getSessionManager().forEachClientSession(session -> {
            var consumerAttribute = session.getConsumerRegister();
            if (consumerAttribute == null) {
                return;
            }
            var providerConfig = consumerAttribute.getProviderConfig();
            if (providerConfig == null) {
                return;
            }
            list.add(consumerAttribute.toProviderSimple());
        });

        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        // Write this consumer's info to an ephemeral node under /consumer
        var localRegisterVO = NetContext.getConfigManager().getLocalConfig().toLocalRegister();
        var path = consumerRootPath + StringUtils.SLASH + localRegisterVO.toConsumerString();
        addData(path, StringUtils.bytes(JsonUtils.object2String(list)), CreateMode.EPHEMERAL);
    }

    @Override
    public void addData(String path, byte[] bytes, CreateMode mode) {
        try {
            var stat = curator.checkExists().forPath(path);

            if (Objects.isNull(stat)) {
                curator.create()
                        .creatingParentsIfNeeded()
                        .withMode(mode)
                        .forPath(path, bytes);
            } else {
                curator.setData().forPath(path, bytes);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete the node at the given path.
     *
     * @param path the ZooKeeper path to delete
     */
    @Override
    public void removeData(String path) {
        try {
            curator.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Query the data stored at the given ZooKeeper path.
     *
     * @param path the ZooKeeper path to read
     * @return the raw bytes stored at the path
     */
    @Override
    public byte[] queryData(String path) {
        try {
            return curator.getData().storingStatIn(new Stat()).forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check whether the given ZooKeeper path exists.
     */
    @Override
    public boolean haveNode(String path) {
        try {
            return Objects.nonNull(curator.checkExists().forPath(path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * List all immediate child paths under the given ZooKeeper path.
     */
    @Override
    public List<String> children(String path) {
        try {
            var children = curator.getChildren().forPath(path)
                    .stream()
                    .filter(it -> StringUtils.isNotBlank(it) && !"null".equals(it))
                    .toList();
            return children;
        } catch (Exception e) {
            logger.error("query children unknown exception", e);
        } catch (Throwable t) {
            logger.error("query children unknown error", t);
        }
        return Collections.emptyList();
    }

    @Override
    public String rootPath() {
        return rootPath;
    }

    /**
     * Query all registered remote providers.
     */
    @Override
    public List<Register> remoteProviderRegisters() {
        try {
            var remoteProviders = children(providerRootPath)
                    .stream()
                    .map(Register::parseString)
                    .filter(Objects::nonNull)
                    .toList();
            return remoteProviders;
        } catch (Exception e) {
            logger.error("remoteProviderRegisters unknown exception", e);
        } catch (Throwable t) {
            logger.error("remoteProviderRegisters unknown error", t);
        }
        return Collections.emptyList();
    }

    /**
     * Register a listener on the given ZooKeeper path that triggers callbacks on data changes.
     * The update callback is invoked on node creation or update; the remove callback on deletion.
     *
     * @param listenerPath   the ZooKeeper path to watch
     * @param updateCallback callback invoked on create/update; first arg is path, second is new data
     * @param removeCallback callback invoked on deletion; arg is the deleted path
     */
    @Override
    public void addListener(String listenerPath, BiConsumer<String, byte[]> updateCallback, Consumer<String> removeCallback) {
        try {
            var providerStat = curator.checkExists().forPath(listenerPath);
            if (Objects.isNull(providerStat)) {
                curator.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(listenerPath, ArrayUtils.EMPTY_BYTE_ARRAY);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var listener = CuratorCache.builder(curator, listenerPath).build();
        listener.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData oldData, ChildData newData) {
                switch (type) {
                    case NODE_CHANGED:
                    case NODE_CREATED:
                        logger.info("listener child updated [oldData:{}] [newData:{}]", childDataToString(oldData), childDataToString(newData));
                        if (updateCallback != null) {
                            try {
                                updateCallback.accept(newData.getPath(), newData.getData());
                            } catch (Exception e) {
                                logger.error("listener child updated error", e);
                            }
                        }
                        break;
                    case NODE_DELETED:
                        if (removeCallback != null) {
                            removeCallback.accept(oldData.getPath());
                        }
                        break;
                    default:
                }
            }
        }, executor);
        listener.start();
        listenerList.add(listener);
    }

    @Override
    public void shutdown() {
        if (curator == null) {
            return;
        }
        try {
            var localRegisterVO = NetContext.getConfigManager().getLocalConfig().toLocalRegister();
            if (curator.getState() == CuratorFrameworkState.STARTED) {
                // Remove the ephemeral provider node on shutdown
                if (Objects.nonNull(localRegisterVO.getProviderConfig())) {
                    var localProviderPath = providerRootPath + StringUtils.SLASH + localRegisterVO.toProviderString();
                    var localProviderStat = curator.checkExists().forPath(localProviderPath);
                    if (Objects.nonNull(localProviderStat)) {
                        curator.delete().guaranteed().deletingChildrenIfNeeded().forPath(localProviderPath);
                    }
                }

                // Remove the ephemeral consumer node on shutdown
                if (Objects.nonNull(localRegisterVO.getConsumerConfig())) {
                    var localConsumerPath = consumerRootPath + StringUtils.SLASH + localRegisterVO.toConsumerString();
                    var localConsumerStat = curator.checkExists().forPath(localConsumerPath);
                    if (Objects.nonNull(localConsumerStat)) {
                        curator.delete().guaranteed().deletingChildrenIfNeeded().forPath(localConsumerPath);
                    }
                }
            }
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getMessage(e));
        }

        try {
            listenerList.forEach(it -> IOUtils.closeIO(it));
            IOUtils.closeIO(providerCuratorCache, curator);
            ThreadUtils.shutdown(executor);
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getMessage(e));
        }
    }

    private String childDataToString(ChildData childData) {
        if (childData == null) {
            return StringUtils.EMPTY;
        }

        // Only print data inline when the payload is small
        if (childData.getData() == null || childData.getData().length <= 8) {
            return childData.toString();
        }

        return StringUtils.format("[path:{}] [stat:{}] [dataSize:{}]", childData.getPath(), childData.getStat(), childData.getData().length);
    }

}
