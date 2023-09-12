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

package com.zfoo.net.protocol;

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.*;
import com.zfoo.net.router.attachment.SignalAttachment;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author godotg
 */
public class ProtocolTest {

    private static final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("config.xml");
    private static final IPacketService packetService = NetContext.getPacketService();

    private static final SignalAttachment attachment = new SignalAttachment();

    private static final ObjectA objectA0 = new ObjectA();
    private static final ObjectA objectA1 = new ObjectA();
    private static final ObjectB objectB = new ObjectB();
    private static final List<Integer> list = List.of(Integer.MIN_VALUE, -99, 0, 99, Integer.MAX_VALUE);
    private static final Map<Integer, String> mapWithIntegerAndString = Map.of(Integer.MIN_VALUE, "min", -99, "-99", 0, "0", 99, "99", Integer.MAX_VALUE, "max");
    private static final Map<Integer, ObjectA> mapWithObject = Map.of(Integer.MIN_VALUE, objectA0, -99, objectA0, 0, objectA0, 99, objectA0, Integer.MAX_VALUE, objectA0);
    private static final List<Map<Integer, ObjectA>> listWithMapWithObject = List.of(mapWithObject, mapWithObject, mapWithObject);

    static {
        objectA0.setA(Integer.MAX_VALUE);
        objectA0.setObjectB(objectB);
        objectA1.setA(Integer.MIN_VALUE);
        objectA1.setObjectB(objectB);
        objectB.setFlag(false);

        attachment.setSignalId(Integer.MAX_VALUE);
    }

    @Test
    public void testCMInt() {
        CM_Int cm = new CM_Int();
        cm.setFlag(false);
        cm.setA(Byte.MIN_VALUE);
        cm.setB(Short.MIN_VALUE);
        cm.setC(Integer.MIN_VALUE);
        cm.setD(Long.MIN_VALUE);
        cm.setE('e');
        cm.setF("Hello Jaysunxiao，this is the World!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        ByteBuf writeBuff = Unpooled.directBuffer();
        packetService.write(writeBuff, cm, attachment);

        writeBuff.readerIndex(PacketService.PACKET_HEAD_LENGTH);// 信息头的长度

        DecodedPacketInfo packetInfo = packetService.read(writeBuff);

        Assert.assertEquals(packetInfo.getPacket(), cm);
        Assert.assertEquals(packetInfo.getAttachment(), attachment);
    }

    @Test
    public void testCmObject() {
        CM_Object cm = new CM_Object();
        cm.setA(Integer.MIN_VALUE);
        cm.setB(objectA0);

        ByteBuf writeBuff = Unpooled.buffer();
        packetService.write(writeBuff, cm, null);

        writeBuff.readerIndex(PacketService.PACKET_HEAD_LENGTH);// 信息头的长度

        DecodedPacketInfo packetInfo = packetService.read(writeBuff);

        Assert.assertEquals(packetInfo.getPacket(), cm);
        Assert.assertNull(packetInfo.getAttachment());
    }

    @Test
    public void testCMFloatMin() {
        CM_Float cm = new CM_Float();
        cm.setA(Float.MIN_VALUE);
        cm.setB(Float.MIN_VALUE);
        cm.setC(Double.MIN_VALUE);
        cm.setD(Double.MIN_VALUE);

        ByteBuf writeBuff = Unpooled.buffer();
        packetService.write(writeBuff, cm, null);

        writeBuff.readerIndex(PacketService.PACKET_HEAD_LENGTH);// 信息头的长度

        DecodedPacketInfo packetInfo = packetService.read(writeBuff);

        Assert.assertEquals(packetInfo.getPacket(), cm);
        Assert.assertNull(packetInfo.getAttachment());
    }

    @Test
    public void testCMFloatNormal() {
        CM_Float cm = new CM_Float();
        cm.setA((float) 0.1);
        cm.setB((float) 0.2);
        cm.setC(1.1);
        cm.setD(100.1);

        ByteBuf writeBuff = Unpooled.buffer();
        packetService.write(writeBuff, cm, null);

        writeBuff.readerIndex(PacketService.PACKET_HEAD_LENGTH);// 信息头的长度

        DecodedPacketInfo packetInfo = packetService.read(writeBuff);

        Assert.assertEquals(packetInfo.getPacket(), cm);
        Assert.assertNull(packetInfo.getAttachment());
    }

    @Test
    public void testCMList() {
        CM_List cm = new CM_List();

        List<List<Integer>> listWithList = new ArrayList<>();
        listWithList.add(list);
        listWithList.add(list);
        listWithList.add(list);
        cm.setListWitList(listWithList);

        List<ObjectA> listWithObject = List.of(objectA0, objectA0, objectA0);
        cm.setObjs(listWithObject);
        cm.setList(list);

        List<List<ObjectA>> listListWithObject = List.of(listWithObject, listWithObject, listWithObject);
        List<List<List<ObjectA>>> listListListWithObject = List.of(listListWithObject, listListWithObject, listListWithObject);
        cm.setListWithObject(listListListWithObject);

        List<Map<Integer, String>> listWithMap = List.of(mapWithIntegerAndString, mapWithIntegerAndString, mapWithIntegerAndString);
        cm.setListWithMap(listWithMap);

        List<List<Map<Integer, ObjectA>>> listListWithMap = List.of(listWithMapWithObject, listWithMapWithObject, listWithMapWithObject);
        cm.setListListWithMap(listListWithMap);

        ByteBuf writeBuff = Unpooled.buffer();
        packetService.write(writeBuff, cm, null);

        writeBuff.readerIndex(PacketService.PACKET_HEAD_LENGTH);// 信息头的长度

        DecodedPacketInfo packetInfo = packetService.read(writeBuff);

        Assert.assertEquals(packetInfo.getPacket(), cm);
        Assert.assertNull(packetInfo.getAttachment());
    }


    @Test
    public void testCM_Set() {
        CM_Set cm = new CM_Set();

        Set<Integer> integerSet = Set.of(Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
        cm.setA(integerSet);

        Set<ObjectA> setObject = Set.of(objectA0, objectA1);
        cm.setB(setObject);
        cm.setC(Set.of(list));

        Set<Set<ObjectA>> setSetObject = Set.of(setObject);
        cm.setSetSetSetWithObject(Set.of(setSetObject));

        cm.setSetWithMap(Set.of(mapWithIntegerAndString));
        cm.setSetListWithMap(Set.of(listWithMapWithObject));


        ByteBuf writeBuff = Unpooled.buffer();
        packetService.write(writeBuff, cm, null);

        writeBuff.readerIndex(PacketService.PACKET_HEAD_LENGTH);// 信息头的长度

        DecodedPacketInfo packetInfo = packetService.read(writeBuff);

        Assert.assertEquals(packetInfo.getPacket(), cm);
        Assert.assertNull(packetInfo.getAttachment());
    }

    @Test
    public void testCMArray() {
        CM_Array cm = new CM_Array();
        int[] a = new int[]{1, 2, 34, 5};
        cm.setA(a);
        ObjectA[] array = new ObjectA[]{objectA0, objectA0, objectA0};
        cm.setB(array);

        ByteBuf writeBuff = Unpooled.buffer();
        packetService.write(writeBuff, cm, null);

        writeBuff.readerIndex(PacketService.PACKET_HEAD_LENGTH);// 信息头的长度

        DecodedPacketInfo packetInfo = packetService.read(writeBuff);

        Assert.assertEquals(packetInfo.getPacket(), cm);
        Assert.assertNull(packetInfo.getAttachment());
    }

    @Test
    public void testCMMap() {
        CM_Map cm = new CM_Map();

        Map<Integer, Integer> map = Map.of(Integer.MIN_VALUE, Integer.MIN_VALUE, -99, -99, 0, 0, 99, 99, Integer.MAX_VALUE, Integer.MAX_VALUE);
        Map<Integer, ObjectA> mapA = Map.of(Integer.MIN_VALUE, objectA0, -99, objectA0, 0, objectA0, 99, objectA0, Integer.MAX_VALUE, objectA0);
        Map<ObjectA, String> mapB = Map.of(objectA0, "objectA0", objectA1, "objectA1");
        Map<ObjectA, ObjectA> mapC = Map.of(objectA0, objectA0, objectA1, objectA1);
        cm.setMap(map);
        cm.setMapA(mapA);
        cm.setMapB(mapB);
        cm.setMapC(mapC);

        Map<List<Integer>, ObjectA> mapWithList = Map.of(list, objectA0);
        Map<List<Integer>, Map<List<Integer>, ObjectA>> mapWithListAndMap = Map.of(list, mapWithList);
        cm.setMapWithListAndMap(mapWithListAndMap);

        ByteBuf writeBuff = Unpooled.buffer();
        packetService.write(writeBuff, cm, null);

        writeBuff.readerIndex(PacketService.PACKET_HEAD_LENGTH);// 信息头的长度

        DecodedPacketInfo packetInfo = packetService.read(writeBuff);

        Assert.assertEquals(packetInfo.getPacket(), cm);
        Assert.assertNull(packetInfo.getAttachment());
    }

}
