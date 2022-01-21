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
import com.zfoo.net.core.tcp.TcpClient;
import com.zfoo.net.core.tcp.TcpServer;
import com.zfoo.net.session.model.AttributeType;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.collection.ConcurrentArrayList;
import com.zfoo.protocol.collection.ConcurrentHashSet;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.manager.SchedulerBus;
import com.zfoo.util.ThreadUtils;
import com.zfoo.util.net.HostAndPort;
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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 服务注册，服务发现
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class ZookeeperRegistry implements IRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private static final String ROOT_PATH = "/zfoo";
    private static final String PROVIDER_ROOT_PATH = ROOT_PATH + "/provider";
    private static final String CONSUMER_ROOT_PATH = ROOT_PATH + "/consumer";

    private static final long RETRY_SECONDS = 5;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(new ConfigThreadFactory());

    private static class ConfigThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        // config-p1-t1 = config-pool-1-thread-1
        ConfigThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "config-p" + poolNumber.getAndIncrement() + "-t";
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new FastThreadLocalThread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            t.setUncaughtExceptionHandler((thread, e) -> logger.error(thread.toString(), e));
            return t;
        }
    }


    private CuratorFramework curator;
    /**
     * provider的监听
     */
    private CuratorCache providerCuratorCache;
    /**
     * consumer需要消费的provider集合
     */
    private final Set<RegisterVO> providerCacheSet = new ConcurrentHashSet<>();
    /**
     * 本地注册信息
     */
    private final RegisterVO localRegisterVO = NetContext.getConfigManager().getLocalConfig().toLocalRegisterVO();


    /**
     * addListener中的cache全部会被添加到这个集合中，这个集合不包括providerCuratorCache
     */
    private final List<CuratorCache> listenerList = new ConcurrentArrayList<>();

    @Override
    public void start() {
        var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();
        if (Objects.isNull(registryConfig)) {
            logger.info("服务提供者没有配置，不会在zk中注册服务，如果是单机启动请忽略这条日志");
            return;
        }

        // 先启动本地服务提供者，再启动curator
        startProvider();

        startCurator();

        startProviderCache();
    }

    private void startProvider() {
        var providerConfig = NetContext.getConfigManager().getLocalConfig().getProvider();

        if (Objects.isNull(providerConfig)) {
            logger.info("服务提供者没有配置，不会在zk中注册服务，如果是单机启动请忽略这条日志");
            return;
        }

        var providerServer = new TcpServer(providerConfig.localHostAndPortOrDefault());
        providerServer.start();
    }

    private void startCurator() {
        var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();

        if (!registryConfig.getCenter().toLowerCase().matches("zookeeper")) {
            throw new IllegalArgumentException(StringUtils
                    .format("[center:{}]注册中心只能是zookeeper", JsonUtils.object2String(registryConfig)));
        }

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
                    case LOST:
                        // 忽略配置中心失去连接，使用本地配置的缓存
                        logger.error("[zookeeper:{}]失去连接，使用缓存", zookeeperConnectStr);
                        break;
                    case SUSPENDED:
                    case READ_ONLY:
                        logger.warn("[zookeeper:{}]忽略的[state{}]", zookeeperConnectStr, state);
                        break;
                    case CONNECTED:
                    case RECONNECTED:
                        createZookeeperRootPath();
                        initZookeeper();
                        break;
                    default:
                        logger.error("[zookeeper:{}]未知状态[state{}]", zookeeperConnectStr, state);
                }
            }
        }, executor);

        curator.start();
        try {
            curator.blockUntilConnected();
        } catch (Throwable t) {
            throw new RuntimeException("启动zookeeper异常", t);
        }
    }

    private void createZookeeperRootPath() {
        try {
            // 创建zookeeper的根路径
            var rootStat = curator.checkExists().forPath(ROOT_PATH);
            if (Objects.isNull(rootStat)) {
                var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();
                var builder = curator.create();
                builder.creatingParentsIfNeeded();
                if (registryConfig.hasZookeeperAuthor()) {
                    var zookeeperAuthorStr = registryConfig.toZookeeperAuthor();
                    var aclList = List.of(new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(zookeeperAuthorStr))));
                    builder.withACL(aclList);
                }
                builder.withMode(CreateMode.PERSISTENT);
                builder.forPath(ROOT_PATH, StringUtils.bytes(registryConfig.getCenter()));
            } else {
                var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();
                var bytes = curator.getData().storingStatIn(new Stat()).forPath(ROOT_PATH);
                var rootPathData = StringUtils.bytesToString(bytes);

                // 检查zookeeper根节点的内容
                if (!rootPathData.equals(registryConfig.getCenter())) {
                    throw new RuntimeException(StringUtils.format("zookeeper的rootPath[{}]内容配置错误[{}]，期望的内容是[{}]，请检查相关节点并重新启动", ROOT_PATH, rootPathData, registryConfig.getCenter()));
                }

                // 检查zookeeper根节点的权限
                if (registryConfig.hasZookeeperAuthor()) {
                    try {
                        var providerRootPathAclList = curator.getACL().forPath(ROOT_PATH);
                        AssertionUtils.notEmpty(providerRootPathAclList);
                        AssertionUtils.isTrue(providerRootPathAclList.size() == 1);
                        var zookeeperAuthorStr = registryConfig.toZookeeperAuthor();
                        var aclList = List.of(new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(zookeeperAuthorStr))));
                        AssertionUtils.isTrue(providerRootPathAclList.get(0).equals(aclList.get(0)));
                    } catch (Exception e) {
                        throw new RuntimeException(StringUtils.format("zookeeper的rootPath[{}]权限配置错误[{}]", ROOT_PATH, ExceptionUtils.getMessage(e)));
                    }
                }

            }


            var providerStat = curator.checkExists().forPath(PROVIDER_ROOT_PATH);
            if (Objects.isNull(providerStat)) {
                curator.create()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(PROVIDER_ROOT_PATH, ArrayUtils.EMPTY_BYTE_ARRAY);
            }

            var consumerStat = curator.checkExists().forPath(CONSUMER_ROOT_PATH);
            if (Objects.isNull(consumerStat)) {
                curator.create()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(CONSUMER_ROOT_PATH, ArrayUtils.EMPTY_BYTE_ARRAY);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void startProviderCache() {
        // 初始化providerCache
        providerCuratorCache = CuratorCache.builder(curator, PROVIDER_ROOT_PATH)
                .withExceptionHandler(e -> {
                    logger.error("providerCuratorCache未知异常", e);
                    initZookeeper();
                })
                .build();

        providerCuratorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData oldData, ChildData newData) {
                switch (type) {
                    case NODE_CHANGED:
                        logger.error("不需要处理的[oldData:{}][newData:{}]", childDataToString(oldData), childDataToString(newData));
                        initZookeeper();
                        break;
                    case NODE_CREATED:
                        var providerStr = StringUtils.substringAfterFirst(newData.getPath(), PROVIDER_ROOT_PATH + StringUtils.SLASH);
                        var provider = RegisterVO.parseString(providerStr);
                        if (RegisterVO.providerHasConsumerModule(provider, localRegisterVO)) {
                            providerCacheSet.add(provider);
                            checkConsumer();
                            logger.info("发现新的订阅服务[{}]", providerStr);
                        }
                        break;
                    case NODE_DELETED:
                        var oldProviderStr = StringUtils.substringAfterFirst(oldData.getPath(), PROVIDER_ROOT_PATH + StringUtils.SLASH);
                        var oldProvider = RegisterVO.parseString(oldProviderStr);
                        if (providerCacheSet.contains(oldProvider)) {
                            providerCacheSet.remove(oldProvider);
                            checkConsumer();
                            logger.info("取消订阅服务[{}]", oldProviderStr);
                        }
                        break;
                    default:
                }
            }

            @Override
            public void initialized() {
                initZookeeper();
            }
        }, executor);

        providerCuratorCache.start();
    }

    private void initZookeeper() {
        executor.execute(() -> {
            try {
                initLocalProvider();

                initConsumerCache();
            } catch (Exception e) {
                logger.error("zookeeper初始化失败，等待[{}]秒，重新初始化", RETRY_SECONDS, e);
                SchedulerBus.schedule(new Runnable() {
                    @Override
                    public void run() {
                        initZookeeper();
                    }
                }, RETRY_SECONDS, TimeUnit.SECONDS);
            }
        });
    }

    private void initLocalProvider() throws Exception {
        if (Objects.nonNull(localRegisterVO.getProviderConfig())) {
            var localProviderVoStr = localRegisterVO.toProviderString();
            var localProviderPath = PROVIDER_ROOT_PATH + StringUtils.SLASH + localProviderVoStr;

            var localProviderStat = curator.checkExists().forPath(localProviderPath);
            if (Objects.isNull(localProviderStat)) {
                curator.create()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(localProviderPath, StringUtils.EMPTY.getBytes());
                logger.info("注册服务成功[{}]", localProviderVoStr);
            } else {
                // 如果服务提供者已经有节点了，防止这个节点是是上次来不及删除的临时节点
                var curatorSessionId = curator.getZookeeperClient().getZooKeeper().getSessionId();
                var providerNodeSessionId = localProviderStat.getEphemeralOwner();
                if (curatorSessionId != providerNodeSessionId) {
                    curator.delete()
                            .guaranteed()
                            .deletingChildrenIfNeeded()
                            .withVersion(localProviderStat.getVersion())
                            .forPath(localProviderPath);
                    throw new RuntimeException(StringUtils.format("curator[sessionId:{}]和providerNode[sessionId:{}]的session不一致"
                            , curatorSessionId, providerNodeSessionId));
                }
            }
        }
    }

    private void initConsumerCache() throws Exception {
        // 初始化providerCacheSet
        var remoteProviderSet = curator.getChildren().forPath(PROVIDER_ROOT_PATH).stream()
                .filter(it -> !StringUtils.isBlank(it) && !"null".equals(it))
                .map(it -> RegisterVO.parseString(it))
                .filter(it -> Objects.nonNull(it))
                .filter(it -> RegisterVO.providerHasConsumerModule(it, localRegisterVO))
                .collect(Collectors.toSet());

        providerCacheSet.clear();
        providerCacheSet.addAll(remoteProviderSet);

        // 初始化consumer，providerCacheSet改变会导致消费者改变
        checkConsumer();
    }

    @Override
    public void checkConsumer() {
        if (curator == null) {
            return;
        }

        if (curator.getState() == CuratorFrameworkState.STOPPED) {
            return;
        }

        executor.execute(() -> doCheckConsumer());
    }

    private void doCheckConsumer() {
        if (curator.getState() != CuratorFrameworkState.STARTED) {
            logger.error("curator还没有启动，忽略本次consumer的检查");
            return;
        }

        logger.info("开始通过[providerCacheSet:{}]检查[consumer:{}]", providerCacheSet, NetContext.getSessionManager().getClientSessionMap().size());

        var recheckFlag = false;

        for (var providerCache : providerCacheSet) {
            var consumerClientList = NetContext.getSessionManager().getClientSessionMap().values().stream()
                    .filter(it -> {
                        var attribute = it.getAttribute(AttributeType.CONSUMER);
                        return Objects.nonNull(attribute) && attribute.equals(providerCache);
                    })
                    .collect(Collectors.toList());

            if (consumerClientList.size() == 1) {
                var consumer = consumerClientList.get(0);
                if (SessionUtils.isActive(consumer)) {
                    continue;
                } else {
                    recheckFlag = true;
                    NetContext.getSessionManager().removeClientSession(consumer);
                    logger.error("[consumer:{}]失去连接，从clientSession中移除", consumer);
                    continue;
                }
            } else if (consumerClientList.size() > 1) {
                logger.error("[consumerClientList:{}]中有多个重复的[RegisterVO:{}]", consumerClientList, providerCache);
                continue;
            }

            var client = new TcpClient(HostAndPort.valueOf(providerCache.getProviderConfig().getAddress()));
            var session = client.start();
            if (Objects.isNull(session)) {
                logger.error("[consumer:{}]启动失败，等待[{}]秒，重新检查consumer", providerCache, RETRY_SECONDS);
                recheckFlag = true;
            } else {
                session.putAttribute(AttributeType.CONSUMER, providerCache);
                EventBus.asyncSubmit(ConsumerStartEvent.valueOf(providerCache, session));

                try {
                    var path = CONSUMER_ROOT_PATH + StringUtils.SLASH + localRegisterVO.toConsumerString();
                    var stat = curator.checkExists().forPath(path);
                    if (Objects.isNull(stat)) {
                        curator.create()
                                .withMode(CreateMode.EPHEMERAL)
                                .forPath(path);
                    } else {
                        curator.setData().forPath(path);
                    }

                } catch (Exception e) {
                    // 因为并不关心consumer的状态，这种失败只需要记录一个错误日志就可以了
                    logger.error("consumer写入zookeeper失败", e);
                }
            }
        }

        if (recheckFlag) {
            SchedulerBus.schedule(new Runnable() {
                @Override
                public void run() {
                    checkConsumer();
                }
            }, RETRY_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    public void addData(String path, byte[] bytes, CreateMode mode) {
        try {
            var providerStat = curator.checkExists().forPath(path);

            if (Objects.isNull(providerStat)) {
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

    @Override
    public void removeData(String path) {
        try {
            curator.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] queryData(String path) {
        try {
            return curator.getData().storingStatIn(new Stat()).forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean haveNode(String path) {
        try {
            return Objects.nonNull(curator.checkExists().forPath(path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> children(String path) {
        try {
            var children = curator.getChildren().forPath(path).stream()
                    .filter(it -> !StringUtils.isBlank(it) && !"null".equals(it))
                    .collect(Collectors.toList());
            return children;
        } catch (Exception e) {
            logger.error("未知异常", e);
        } catch (Throwable t) {
            logger.error("未知错误", t);
        }
        return Collections.emptyList();
    }

    @Override
    public Set<RegisterVO> remoteProviderRegisterSet() {
        try {
            var remoteProviderSet = curator.getChildren().forPath(PROVIDER_ROOT_PATH).stream()
                    .filter(it -> !StringUtils.isBlank(it) && !"null".equals(it))
                    .map(it -> RegisterVO.parseString(it))
                    .filter(it -> Objects.nonNull(it))
                    .collect(Collectors.toSet());
            return remoteProviderSet;
        } catch (Exception e) {
            logger.error("未知异常", e);
        } catch (Throwable t) {
            logger.error("未知错误", t);
        }
        return Collections.emptySet();
    }

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
            if (curator.getState() == CuratorFrameworkState.STARTED) {
                // 删除服务提供者的临时节点
                if (Objects.nonNull(localRegisterVO.getProviderConfig())) {
                    var localProviderPath = PROVIDER_ROOT_PATH + StringUtils.SLASH + localRegisterVO.toProviderString();
                    var localProviderStat = curator.checkExists().forPath(localProviderPath);
                    if (Objects.nonNull(localProviderStat)) {
                        curator.delete().guaranteed().deletingChildrenIfNeeded().forPath(localProviderPath);
                    }
                }

                // 删除服务消费者的临时节点
                if (Objects.nonNull(localRegisterVO.getConsumerConfig())) {
                    var localConsumerPath = CONSUMER_ROOT_PATH + StringUtils.SLASH + localRegisterVO.toConsumerString();
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

        // 只打印data数据比较小的内容
        if (childData.getData() == null || childData.getData().length <= 8) {
            return childData.toString();
        }

        return StringUtils.format("[path:{}] [stat:{}] [dataSize:{}]", childData.getPath(), childData.getStat(), childData.getData().length);
    }

}
