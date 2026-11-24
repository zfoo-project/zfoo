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

package com.zfoo.net.packet;

import com.zfoo.net.config.model.NetConfig;
import com.zfoo.net.packet.common.Pong;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.protocol.generate.GenerateOperation;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;

public class ProtocolPackagesTest {
    private void init(NetConfig config) {
        PacketService.initProtocols(config, new DefaultResourceLoader(), GenerateOperation.NO_OPERATION);
    }

    @Test
    public void roundTripWithoutProtocolXml() {
        var config = new NetConfig();
        var common = Pong.class.getPackageName();
        config.setProtocolPackages(List.of(common, SignalAttachment.class.getPackageName(), common));
        init(config);

        var packets = new PacketService();
        var buffer = Unpooled.buffer();
        try {
            var signal = new SignalAttachment();
            packets.write(buffer, Pong.valueOf(123456789L), signal);
            var decoded = packets.read(buffer);
            Assert.assertEquals(123456789L, ((Pong) decoded.getPacket()).getTime());
            Assert.assertEquals(signal.getSignalId(), ((SignalAttachment) decoded.getAttachment()).getSignalId());
            Assert.assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void requiresExactlyOneSource() {
        var config = new NetConfig();
        Assert.assertThrows(IllegalArgumentException.class, () -> init(config));
        config.setProtocolLocation("protocol.xml");
        config.setProtocolPackages(List.of(Pong.class.getPackageName()));
        Assert.assertThrows(IllegalArgumentException.class, () -> init(config));
    }

    @Test
    public void rejectsBlankOrUnmatchedPackages() {
        var config = new NetConfig();
        config.setProtocolPackages(List.of(" "));
        Assert.assertThrows(IllegalArgumentException.class, () -> init(config));
        config.setProtocolPackages(List.of("example.no.such.protocols"));
        Assert.assertThrows(IllegalArgumentException.class, () -> init(config));
    }
}
