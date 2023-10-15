#ifndef ZFOO_SPEED_TEST_H
#define ZFOO_SPEED_TEST_H

#include <iostream>
#include <fstream>
#include <string>

#include "zfoocpp//ProtocolManager.h"

namespace speed_test {

    using namespace zfoo;
    using namespace std;

    SimpleObject *pSimpleObject = nullptr;
    NormalObject *pNormalObject = nullptr;
    ComplexObject *pComplexObject = nullptr;

    int benchmark = 100000;

    void parseObject() {
        // 读取二进制文件
        ifstream file("D:\\github\\zfoo\\protocol\\src\\test\\resources\\complexObject.bytes", ios::out | ios::binary);
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
        pComplexObject = (ComplexObject *) read(newBuffer);

        pSimpleObject = new SimpleObject();
        pSimpleObject->c = pComplexObject->c;
        pSimpleObject->g = pComplexObject->g;

        pNormalObject = new NormalObject();
        pNormalObject->a = pComplexObject->a;
        pNormalObject->aaa = pComplexObject->aaa;
        pNormalObject->b = pComplexObject->b;
        pNormalObject->c = pComplexObject->c;
        pNormalObject->d = pComplexObject->d;
        pNormalObject->e = pComplexObject->e;
        pNormalObject->f = pComplexObject->f;
        pNormalObject->g = pComplexObject->g;
        pNormalObject->jj = pComplexObject->jj;
        pNormalObject->kk = pComplexObject->kk;
        pNormalObject->l = pComplexObject->l;
        pNormalObject->ll = {static_cast<int64_t>(0x8000000000000000LL), -9999999999999999L, -99999999L, -99L, 0L, 99L,
                             99999999L, 9999999999999999L, 0x7fffffffffffffffLL};
        pNormalObject->lll.emplace_back(pComplexObject->kk);
        pNormalObject->lll.emplace_back(pComplexObject->kk);
        pNormalObject->lll.emplace_back(pComplexObject->kk);
        pNormalObject->llll = pComplexObject->llll;
        pNormalObject->m = pComplexObject->m;
        pNormalObject->mm = pComplexObject->mm;
        pNormalObject->s = pComplexObject->s;
        pNormalObject->ssss = pComplexObject->ssss;
    }

    void zfooTest() {
        ByteBuffer buffer;

        clock_t start = clock();
        for (int i = 0; i < benchmark; i++) {
            buffer.clear();
            write(buffer, pSimpleObject);
            auto packet = read(buffer);
            delete packet;
        }
        cout << "[zfoo]     [简单对象][size:" << buffer.writerIndex() << "] [time:" << (clock() - start) << "]" << endl;

        start = clock();
        for (int i = 0; i < benchmark; i++) {
            buffer.clear();
            write(buffer, pNormalObject);
            auto packet = read(buffer);
            delete packet;
        }
        cout << "[zfoo]     [常规对象][size:" << buffer.writerIndex() << "] [time:" << (clock() - start) << "]" << endl;

        start = clock();
        for (int i = 0; i < benchmark; i++) {
            buffer.clear();
            write(buffer, pComplexObject);
            auto packet = read(buffer);
            delete packet;
        }
        cout << "[zfoo]     [复杂对象][size:" << buffer.writerIndex() << "] [time:" << (clock() - start) << "]" << endl;
    }

    void singleThreadBenchmarks() {
        if (benchmark <= 0 || benchmark >= 1000000000) {
            return;
        }
        cout << "[单线程性能测试-->[benchmark:" << benchmark << "]]" << endl;
        zfooTest();
        benchmark = benchmark * 2;
        singleThreadBenchmarks();
    }

}

#endif
