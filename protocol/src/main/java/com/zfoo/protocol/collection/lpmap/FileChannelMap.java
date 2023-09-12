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

package com.zfoo.protocol.collection.lpmap;

import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.registration.IProtocolRegistration;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author godotg
 */
public class FileChannelMap<V> implements LpMap<V>, Closeable {

    private final File dbFile;
    protected RandomAccessFile dbFileRandomAccess;
    protected FileChannel dbFileChannel;

    private final File indexFile;
    protected RandomAccessFile indexFileRandomAccess;
    protected FileChannel indexFileChannel;

    protected long maxIndex;

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

            var protocolId = ProtocolManager.protocolId(clazz);
            protocolRegistration = ProtocolManager.getProtocol(protocolId);

            indexBuffer = ByteBufAllocator.DEFAULT.ioBuffer(16);
            dbBuffer = ByteBufAllocator.DEFAULT.ioBuffer(100);

            maxIndex = indexFileChannel.size() / 16;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public V put(long key, V packet) {
        checkKey(key);

        V previousValue = null;
        if (key <= maxIndex) {
            previousValue = get(key);
        } else {
            maxIndex = key;
        }

        setKeyValue(key, packet);
        return previousValue;
    }

    @Override
    public V delete(long key) {
        checkKey(key);

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

        if (key > maxIndex) {
            return null;
        }

        try {
            clearByteBuf();
            indexBuffer.writeBytes(indexFileChannel, key * 16L, 16);
            var packetPosition = indexBuffer.readLong();
            var packetSize = indexBuffer.readLong();
            if (packetSize <= 0) {
                return null;
            }

            dbBuffer.writeBytes(dbFileChannel, packetPosition, (int) packetSize);
            var packet = protocolRegistration.read(dbBuffer);
            @SuppressWarnings("unchecked")
            var p = (V) packet;
            return p;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取从startKey到endKey的值
     *
     * @param startKey inclusive
     * @param endKey   exclusive
     * @return list
     */
    public List<V> getFrom(long startKey, long endKey) {
        checkKey(startKey);
        checkKey(endKey);

        if (startKey >= endKey) {
            throw new RunException("range error startKey < endKey");
        }
        if (startKey > maxIndex) {
            return Collections.emptyList();
        }

        var list = new ArrayList<V>();
        for (var i = startKey; i < endKey; i++) {
            var value = get(i);
            if (value != null) {
                list.add(value);
            }
        }
        return list;
    }


    @Override
    public long getMaxIndex() {
        return maxIndex;
    }

    @Override
    public long getIncrementIndex() {
        maxIndex++;
        return maxIndex;
    }

    @Override
    public void forEach(BiConsumer<Long, V> biConsumer) {
        for (var i = 0L; i <= getMaxIndex(); i++) {
            var value = get(i);
            if (value != null) {
                biConsumer.accept(i, value);
            }
        }
    }

    @Override
    public void clear() {
        try {
            maxIndex = 0;
            indexFileRandomAccess.setLength(0);
            dbFileRandomAccess.setLength(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() throws IOException {
        IOUtils.closeIO(indexFileRandomAccess, indexFileChannel, dbFileRandomAccess, dbFileChannel);
        ReferenceCountUtil.release(indexBuffer);
        ReferenceCountUtil.release(dbBuffer);
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

    protected void clearByteBuf() {
        indexBuffer.clear();
        dbBuffer.clear();
    }

}
