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

package com.zfoo.net.core.provider;

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.provider.ProviderMessAnswer;
import com.zfoo.net.packet.provider.ProviderMessAsk;
import com.zfoo.net.util.SessionUtilsTest;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author godotg
 */
@Ignore
public class ProviderTest {

    private static final Logger logger = LoggerFactory.getLogger(ProviderTest.class);

    /**
     * RPC tutorial:
     * 1. Ensure ZooKeeper is started first
     * 2. Start the service providers: startProvider0, startProvider1, startProvider2
     * 3. Start the service consumers: startSyncRandomConsumer, startAsyncRandomConsumer, startConsistentHashConsumer, startCachedConsistentHashConsumer
     * 4. Each consumer uses a different load-balancing strategy
     */
    @Test
    public void startProvider0() {
        var context = new ClassPathXmlApplicationContext("provider/provider_config.xml");
        SessionUtilsTest.printSessionInfo();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void startProvider1() {
        var context = new ClassPathXmlApplicationContext("provider/provider_config.xml");
        SessionUtilsTest.printSessionInfo();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    @Test
    public void startProvider2() {
        var context = new ClassPathXmlApplicationContext("provider/provider_config.xml");
        SessionUtilsTest.printSessionInfo();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    /**
     * Random load-balance, synchronous request mode
     */
    @Test
    public void startSyncRandomConsumer() throws Exception {
        var context = new ClassPathXmlApplicationContext("provider/consumer_random_config.xml");
        SessionUtilsTest.printSessionInfo();

        var ask = new ProviderMessAsk();
        ask.setMessage("Hello, this is the consumer!");
        for (int i = 0; i < 1000; i++) {
            ThreadUtils.sleep(1000);
            var response = NetContext.getConsumer().syncAsk(ask, ProviderMessAnswer.class, null).packet();
            logger.info("consumer request[{}] received response[{}]", i, JsonUtils.object2String(response));
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    /**
     * Random load-balance, asynchronous request mode
     */
    @Test
    public void startAsyncRandomConsumer() {
        var context = new ClassPathXmlApplicationContext("provider/consumer_random_config.xml");
        SessionUtilsTest.printSessionInfo();

        var ask = new ProviderMessAsk();
        ask.setMessage("Hello, this is the consumer!");
        var atomicInteger = new AtomicInteger(0);

        for (int i = 0; i < 1000; i++) {
            ThreadUtils.sleep(1000);
            NetContext.getConsumer().asyncAsk(ask, ProviderMessAnswer.class, null).whenComplete(answer -> {
                logger.info("consumer request[{}] received response[{}]", atomicInteger.incrementAndGet(), JsonUtils.object2String(answer));
            });
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    /**
     * Consistent-hash load-balancing consumer
     */
    @Test
    public void startConsistentHashConsumer() {
        var context = new ClassPathXmlApplicationContext("provider/consumer_consistent_hash_config.xml");
        SessionUtilsTest.printSessionInfo();

        var ask = new ProviderMessAsk();
        ask.setMessage("Hello, this is the consumer!");
        var atomicInteger = new AtomicInteger(0);

        for (int i = 0; i < 1000; i++) {
            ThreadUtils.sleep(1000);
            NetContext.getConsumer().asyncAsk(ask, ProviderMessAnswer.class, 100).whenComplete(answer -> {
                logger.info("consumer request[{}] received response[{}]", atomicInteger.incrementAndGet(), JsonUtils.object2String(answer));
            });
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    /**
     * Cached consistent-hash load-balancing consumer
     */
    @Test
    public void startCachedConsistentHashConsumer() {
        var context = new ClassPathXmlApplicationContext("provider/consumer_cached_consistent_config.xml");
        SessionUtilsTest.printSessionInfo();

        var ask = new ProviderMessAsk();
        ask.setMessage("Hello, this is the consumer!");
        var atomicInteger = new AtomicInteger(0);

        for (int i = 0; i < 1000; i++) {
            ThreadUtils.sleep(1000);
            NetContext.getConsumer().asyncAsk(ask, ProviderMessAnswer.class, 100).whenComplete(answer -> {
                logger.info("consumer request[{}] received response[{}]", atomicInteger.incrementAndGet(), JsonUtils.object2String(answer));
            });
        }

        ThreadUtils.sleep(Long.MAX_VALUE);
    }
}
