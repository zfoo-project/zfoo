#ifndef ZFOO_SERIALIZATION_TEST_H
#define ZFOO_SERIALIZATION_TEST_H

#include <iostream>
#include <fstream>
#include <string>
#include <map>

#include "cppProtocol/ProtocolManager.h"


namespace serialization_test {
    using namespace zfoo;
    using namespace std;

    void objectBTest() {
        ByteBuffer buffer;
        ObjectB objectB;
        objectB.flag = true;

        write(buffer, &objectB);

        ObjectB obj = *((ObjectB *) read(buffer));
        cout << "Hello, World!" << endl;
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
        cout << "Hello, World!" << endl;
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
        cout << "Hello, World!" << endl;
    }


    void complexObjectTest() {
        // 读取二进制文件
        ifstream file("../../resources/ComplexObject.bytes", ios::out | ios::binary);
        unsigned char carray[10000];
        int length = 0;
        while (file.read((char *) &carray[length], sizeof(unsigned char))) {
            length++;
        }
        file.close();

        ByteBuffer buffer;
        buffer.writeBytes(reinterpret_cast<const int8_t *>(carray), length);
        ComplexObject obj = *((ComplexObject *) read(buffer));

        ByteBuffer newBuffer;
        write(newBuffer, &obj);
        obj = *((ComplexObject *) read(newBuffer));
        cout << "Hello, World!" << endl;

        for (int i = 0; i < 3000; ++i) {
            auto a = buffer.getByte(i);
            auto b = newBuffer.getByte(i);
            if (a != b) {
                cout << i << endl;
            }
        }
    }


    void protocol_all_test() {
        try {
            initProtocol();
            objectBTest();
            objectATest();
            normalObjectTest();
            complexObjectTest();
        } catch (string &e) {
            cout << e << endl;
        } catch (...) {
            cout << "unknown" << endl;
        }
    }

}
#endif
