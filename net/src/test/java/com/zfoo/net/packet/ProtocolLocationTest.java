/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.net.packet;

import com.zfoo.net.packet.tcp.TcpHelloRequest;
import com.zfoo.net.packet.udp.UdpHelloRequest;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.generate.GenerateOperation;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

public class ProtocolLocationTest {
    @Test
    public void multiplePackagesWorkWithoutSpringBoot() {
        try (var context = new GenericApplicationContext()) {
            context.refresh();
            var service = new PacketService();
            service.initProtocols(context, List.of(
                    UdpHelloRequest.class.getPackageName(),
                    TcpHelloRequest.class.getPackageName()), GenerateOperation.NO_OPERATION);
            Assert.assertTrue(ProtocolManager.isProtocolClass(TcpHelloRequest.class));

            var buffer = Unpooled.buffer();
            try {
                var request = new UdpHelloRequest();
                request.setMessage("hello");
                var signal = new SignalAttachment();
                service.write(buffer, request, signal);
                var decoded = service.read(buffer);
                Assert.assertEquals("hello", ((UdpHelloRequest) decoded.getPacket()).getMessage());
                Assert.assertEquals(signal.getSignalId(), ((SignalAttachment) decoded.getAttachment()).getSignalId());
            } finally {
                buffer.release();
            }
        }
    }
}
