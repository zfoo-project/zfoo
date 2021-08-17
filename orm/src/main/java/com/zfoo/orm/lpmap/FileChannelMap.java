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
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class FileChannelMap<V extends IPacket> implements LpMap<V>, Closeable {

    private File dbFile;
    protected RandomAccessFile dbFileRandomAccess;
    protected FileChannel dbFileChannel;

    private File indexFile;
    protected RandomAccessFile indexFileRandomAccess;
    protected FileChannel indexFileChannel;


    protected IProtocolRegistration protocolRegistration;

    protected ByteBuf indexBuffer;
    protected ByteBuf dbBuffer;

    public FileChannelMap(String dbPath, Class<V> clazz) {
        try {
            this.dbFile = FileUtils.getOrCreateFile(dbPath, StringUtils.format("{}.db", clazz.getSimpleName()));
            this.dbFileRandomAccess = new RandomAccessFile(dbFile, "rw");
            this.dbFileChannel = this.dbFileRandomAccess.getChannel();

            this.indexFile = FileUtils.getOrCreateFile(dbPath, StringUtils.format("{}.index", clazz.getSimpleName()));
            this.indexFileRandomAccess = new RandomAccessFile(indexFile, "rw");
            this.indexFileChannel = this.indexFileRandomAccess.getChannel();

            if (indexFileChannel.size() <= 0) {
                indexFileRandomAccess.writeLong(0L);
                indexFileRandomAccess.writeLong(0L);
            }

            var protocolId = ProtocolAnalysis.getProtocolIdByClass(clazz);
            protocolRegistration = ProtocolManager.getProtocol(protocolId);

            indexBuffer = ByteBufAllocator.DEFAULT.ioBuffer(16);
            dbBuffer = ByteBufAllocator.DEFAULT.ioBuffer(100);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public long insert(V value) {
        var maxIndex = getMaxIndex() + 1;

        // index索引文件的头16个字节是当前index的大小
        setMaxIndex(maxIndex);

        setKeyValue(maxIndex, value);
        return maxIndex;
    }

    @Override
    public V put(long key, V packet) {
        checkKey(key);

        var maxIndex = getMaxIndex();
        V previousValue = null;
        if (key <= maxIndex) {
            previousValue = get(key);
        } else {
            for (var i = maxIndex + 1; i < key; i++) {
                resetKey(i);
            }
            maxIndex = key;
            setMaxIndex(maxIndex);
        }

        setKeyValue(key, packet);
        return previousValue;
    }

    @Override
    public V delete(long key) {
        checkKey(key);
        var maxIndex = getMaxIndex();

        if (key <= maxIndex) {
            var previousValue = get(key);
            resetKey(key);
            return previousValue;
        } else {
            return null;
        }
    }

    @Override
    public V get(long key) {
        checkKey(key);

        var maxIndex = getMaxIndex();
        if (key > maxIndex) {
            return null;
        }

        try {
            indexBuffer.writeBytes(indexFileChannel, key * 16L, 16);
            var packetPosition = indexBuffer.readLong();
            var packetSize = indexBuffer.readLong();
            if (packetSize <= 0) {
                return null;
            }

            dbBuffer.writeBytes(dbFileChannel, packetPosition, (int) packetSize);
            var packet = protocolRegistration.read(dbBuffer);
            return (V) packet;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public long getMaxIndex() {
        try {
            clearByteBuf();
            indexBuffer.writeBytes(indexFileChannel, 0, 8);
            return indexBuffer.readLong();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            clearByteBuf();
        }
    }

    protected void setKeyValue(long key, V value) {
        try {
            clearByteBuf();

            protocolRegistration.write(dbBuffer, value);

            // db文件
            var packetPosition = dbFileChannel.size();
            // db文件数据的起始位置
            indexBuffer.writeLong(packetPosition);
            // db文件的值的大小
            indexBuffer.writeLong(dbBuffer.readableBytes());
            indexFileChannel.write(indexBuffer.nioBuffer(), key * 16);
            dbFileChannel.write(dbBuffer.nioBuffer(), packetPosition);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            clearByteBuf();
        }
    }

    protected void resetKey(long key) {
        try {
            clearByteBuf();

            indexBuffer.writeLong(0L);
            indexBuffer.writeLong(0L);
            indexFileChannel.write(indexBuffer.nioBuffer(), key * 16);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            clearByteBuf();
        }
    }

    protected void setMaxIndex(long maxIndex) {
        try {
            clearByteBuf();
            indexBuffer.writeLong(maxIndex);
            indexFileChannel.write(indexBuffer.nioBuffer(), 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            clearByteBuf();
        }
    }

    protected void clearByteBuf() {
        indexBuffer.clear();
        dbBuffer.clear();
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeIO(indexFileRandomAccess, indexFileChannel, dbFileRandomAccess, dbFileChannel);
        ReferenceCountUtil.release(indexBuffer);
        ReferenceCountUtil.release(dbBuffer);
    }

}
