#ifndef ZFOO_SERIALIZATION_TEST_H
#define ZFOO_SERIALIZATION_TEST_H

#include <iostream>
#include <fstream>
#include <string>
#include <map>

#include "zfoocpp/ProtocolManager.h"


namespace serialization_test {
    using namespace zfoo;
    using namespace std;

    void objectBTest() {
        ByteBuffer buffer;
        ObjectB objectB;
        objectB.flag = true;

        write(buffer, &objectB);

        ObjectB obj = *((ObjectB *) read(buffer));
        cout << "objectBTest" << endl;
    }

    void objectATest() {
        ByteBuffer buffer;
        ObjectA objectA;

        ObjectB objectB;
        objectB.flag = true;

        map<int32_t, string> map;
        map.insert(pair<int, string>(1, "a"));
        map.insert(pair<int, string>(2, "b"));
        map.insert(pair<int, string>(3, "c"));

        objectA.a = 1;
        objectA.m = map;
        objectA.objectB = objectB;

        write(buffer, &objectA);

        ObjectA obj = *((ObjectA *) read(buffer));
        cout << "objectATest" << endl;
    }


    void normalObjectTest() {
        ByteBuffer buffer;

        ObjectA objectA;
        ObjectB objectB;
        objectB.flag = true;

        map<int32_t, string> map;
        map.insert(pair<int, string>(1, "a"));
        map.insert(pair<int, string>(2, "b"));
        map.insert(pair<int, string>(3, "c"));

        objectA.a = 1;
        objectA.m = map;
        objectA.objectB = objectB;

        NormalObject normalObject;
        normalObject.kk = objectA;
        normalObject.m = map;

        write(buffer, &normalObject);
        NormalObject *p = (NormalObject *) read(buffer);
        cout << "normalObjectTest" << endl;
    }


    void compatibleTest() {
        // 读取二进制文件
//        ifstream file("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-no-compatible.bytes", ios::out | ios::binary);
//        ifstream file("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-compatible.bytes", ios::out | ios::binary);
//        ifstream file("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-inner-compatible.bytes", ios::out | ios::binary);
        ifstream file("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-compatible.bytes", ios::out | ios::binary);
//        ifstream file("D:\\Project\\zfoo\\protocol\\src\\test\\resources\\compatible\\normal-out-inner-inner-compatible.bytes", ios::out | ios::binary);
        unsigned char carray[10000];
        int length = 0;
        while (file.read((char *) &carray[length], sizeof(unsigned char))) {
            length++;
        }
        file.close();

        ByteBuffer buffer;
        buffer.writeBytes(reinterpret_cast<const int8_t *>(carray), length);
        NormalObject obj = *((NormalObject *) read(buffer));

        ByteBuffer newBuffer;
        write(newBuffer, &obj);
        obj = *((NormalObject *) read(newBuffer));

        cout << "source size " << length << endl;
        cout << "target size " << newBuffer.writerIndex() << endl;
        cout << "compatible test" << endl;
    }


    void protocol_all_test() {
        initProtocol();
        compatibleTest();
    }

}
#endif
