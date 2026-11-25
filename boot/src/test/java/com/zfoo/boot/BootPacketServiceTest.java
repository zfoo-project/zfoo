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

package com.zfoo.boot;

import com.zfoo.net.packet.PacketService;
import com.zfoo.net.packet.common.Ping;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.anno.Protocol;
import com.zfoo.protocol.generate.GenerateOperation;
import example.scan.ScanApplication;
import example.scan.messages.Echo;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.io.FileNotFoundException;

public class BootPacketServiceTest {

    @Protocol(id = 2001)
    public record OutsideRoot() {
    }

    @Test
    public void discoversMessagesBelowBootRootWithoutRegisteringBeans() {
        try (var context = new AnnotationConfigApplicationContext(ScanApplication.class)) {
            Assert.assertTrue(ProtocolManager.isProtocolClass(Ping.class));
            Assert.assertFalse(ProtocolManager.isProtocolClass(OutsideRoot.class));
            Assert.assertTrue(context.getBeansOfType(Echo.class).isEmpty());

            var service = context.getBean(PacketService.class);
            var buffer = Unpooled.buffer();
            try {
                var echo = new Echo("你好");
                var signal = new SignalAttachment();
                service.write(buffer, echo, signal);
                var decoded = service.read(buffer);
                Assert.assertEquals(echo, decoded.getPacket());
                Assert.assertEquals(signal.getSignalId(), ((SignalAttachment) decoded.getAttachment()).getSignalId());
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    public void explicitLocationNeverFallsBackToScanning() {
        try (var context = new GenericApplicationContext()) {
            AutoConfigurationPackages.register(context, ScanApplication.class.getPackageName());
            context.refresh();
            var error = Assert.assertThrows(RuntimeException.class, () -> new BootPacketService()
                    .initProtocols(context, "missing-protocol.xml", GenerateOperation.NO_OPERATION));
            Assert.assertTrue(error.getCause() instanceof FileNotFoundException);
        }
    }

    @Test
    public void missingBootRootsDoNotTriggerAClasspathWideScan() {
        try (var context = new GenericApplicationContext()) {
            context.refresh();
            var error = Assert.assertThrows(IllegalStateException.class, () -> new BootPacketService()
                    .initProtocols(context, null, GenerateOperation.NO_OPERATION));
            Assert.assertTrue(error.getMessage().contains("protocolLocation"));
        }
    }
}
