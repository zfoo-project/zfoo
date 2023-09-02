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

package com.zfoo.net.core.http;

import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.packet.DecodedPacketInfo;
import com.zfoo.net.packet.http.HttpHelloRequest;
import com.zfoo.net.router.attachment.HttpAttachment;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ThreadUtils;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.function.Function;

/**
 * @author godotg
 * @version 3.0
 */
@Ignore
public class HttpServerTest {

    /**
     * 访问下面这个网址：
     * <p>
     * http://127.0.0.1:9000/hello
     */
    @Test
    public void startServer() {
        var context = new ClassPathXmlApplicationContext("config.xml");

        var server = new HttpServer(HostAndPort.valueOf("127.0.0.1:9000"), new Function<FullHttpRequest, DecodedPacketInfo>() {
            @Override
            public DecodedPacketInfo apply(FullHttpRequest fullHttpRequest) {
                var uri = StringUtils.trim(fullHttpRequest.uri());
                var attachment = HttpAttachment.valueOf(fullHttpRequest, HttpResponseStatus.OK);
                if (uri.equals("/hello")) {
                    return DecodedPacketInfo.valueOf(HttpHelloRequest.valueOf("other param"), attachment);
                } else {
                    throw new RunException("未知的http路径[{}]", uri);
                }
            }
        });
        server.start();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
