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

package com.zfoo.orm.lpmap;

import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.IOUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class FileHeapMap<V extends IPacket> implements LpMap<V> {

    private File file;

    private IProtocolRegistration protocolRegistration;

    private HeapMap<V> heapMap;

    public FileHeapMap(File file, int initialCapacity, Class<V> clazz) {
        this.file = file;
        var protocolId = ProtocolAnalysis.getProtocolIdByClass(clazz);
        protocolRegistration = ProtocolManager.getProtocol(protocolId);
        heapMap = new HeapMap<>(initialCapacity);

        load();
    }


    @Override
    public long insert(V packet) {
        return heapMap.insert(packet);
    }

    @Override
    public V put(long key, V packet) {
        return heapMap.put(key, packet);
    }

    @Override
    public V delete(long key) {
        return heapMap.delete(key);
    }

    @Override
    public V get(long key) {
        return heapMap.get(key);
    }

    private void load() {
        FileInputStream fileInputStream = null;
        FileChannel fileChannel = null;
        ByteBuf buffer = null;
        try {
            fileInputStream = FileUtils.openInputStream(file);
            fileChannel = fileInputStream.getChannel();

            buffer = ByteBufAllocator.DEFAULT.ioBuffer(1000);
            buffer.writeBytes(fileChannel, 0L, (int) file.length());

            var size = ByteBufUtils.readLong(buffer);
            for (int i = 0; i < size; i++) {
                var key = ByteBufUtils.readLong(buffer);
                var value = (V) protocolRegistration.read(buffer);
                put(key, value);
            }
        } catch (IOException e) {
            IOUtils.closeIO(fileChannel, fileInputStream);
            ReferenceCountUtil.release(buffer);
        }
    }

    public void save() {
        FileOutputStream fileOutputStream = null;
        ByteBuf buffer = null;
        try {
            fileOutputStream = FileUtils.openOutputStream(file, false);
            buffer = ByteBufAllocator.DEFAULT.heapBuffer(1000);

            // 写入长度
            ByteBufUtils.writeLong(buffer, heapMap.map.size());
            buffer.readBytes(fileOutputStream, buffer.readableBytes());

            for (var entry : heapMap.map.entries()) {
                buffer.clear();

                var key = entry.key();
                var value = entry.value();
                ByteBufUtils.writeLong(buffer, key);
                protocolRegistration.write(buffer, value);

                buffer.readBytes(fileOutputStream, buffer.readableBytes());
            }
        } catch (IOException e) {
            IOUtils.closeIO(fileOutputStream);
            ReferenceCountUtil.release(buffer);
        }
    }
}
