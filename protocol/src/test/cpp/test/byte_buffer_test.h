#ifndef ZFOO_BYTE_BUFFER_TEST_H
#define ZFOO_BYTE_BUFFER_TEST_H

#include <string>

#include "zfoocpp/ByteBuffer.h"

namespace byte_buffer_test {

    using namespace zfoo;

    void assert(bool flag) {
        if (!flag) {
            throw "exception happen";
        }
    }

    void typeTest() {
        using namespace std;
        int32_t a = 0xFFFFFFFF;
        uint32_t b = 0xFFFFFFFF;
        assert(a == b);

        b = a;
        assert(a == b);
    }

    void boolTest(ByteBuffer &buffer) {
        bool value = true;
        buffer.writeBool(value);
        assert(buffer.readBool() == value);

        value = false;
        buffer.writeBool(value);
        assert(buffer.readBool() == value);
    }

    void byteTest(ByteBuffer &buffer) {
        int8_t values[] = {127, 0 - 128};
        for (auto value : values) {
            buffer.writeByte(value);
            assert(buffer.readByte() == value);
        }
    }

    void bytesTest(ByteBuffer &buffer) {
        int8_t values[] = {127, 0 - 128};
        int length = sizeof(values) / sizeof(int8_t);
        buffer.writeBytes(values, length);
        int8_t *bytes = buffer.readBytes(length);
        for (int i = 0; i < length; ++i) {
            assert(bytes[i] == values[i]);
        }
    }

    void shortTest(ByteBuffer &buffer) {
        int16_t values[] = {-32768, -100, -2, -1, 0, 1, 2, 100, 32767};
        for (auto value : values) {
            buffer.writeShort(value);
            assert(buffer.readShort() == value);
        }
    }

    void intTest(ByteBuffer &buffer) {
        int32_t values[] = {static_cast<int32_t>(0x80000000), -99999999, -9999, -100, -2, -1, 0, 1, 2, 100, 9999,
                            99999999, 0x7fffffff};

        for (auto value : values) {
            buffer.writeInt(value);
            assert(buffer.readInt() == value);
        }
    }

    void longTest(ByteBuffer &buffer) {
        int64_t values[] = {static_cast<int64_t>(0x8000000000000000LL), -9999999999999999L, -9999999999999999L,
                            -99999999, -9999, -100, -2, -1, 0, 1, 2, 100, 9999, 99999999, 9999999999999999L,
                            0x7fffffffffffffffLL};

        for (auto value : values) {
            buffer.writeLong(value);
            assert(buffer.readLong() == value);
        }
    }

    void floatTest(ByteBuffer &buffer) {
        float values[] = {-12345678.12345678, -1234.5678, -100, -2, -1, 0, 1, 2, 100, 1234.5678, 12345678.12345678};
        for (auto value : values) {
            buffer.writeFloat(value);
            assert(std::abs(buffer.readFloat() - value) < 0.001);
        }
    }

    void doubleTest(ByteBuffer &buffer) {
        double values[] = {-1234.5678, -100, -2, -1, 0, 1, 2, 100, 1234.5678};
        for (auto value : values) {
            buffer.writeDouble(value);
            assert(std::abs(buffer.readDouble() - value) < 0.001);
        }
    }

    void stringTest(ByteBuffer &buffer) {
        using namespace std;
        string value = "hello world!";
        buffer.writeString(value);
        assert(buffer.readString().compare(value) == 0);
    }


    void byte_buffer_all_test() {
        using namespace std;

        typeTest();

        ByteBuffer buffer;
        assert(buffer.getCapacity() == DEFAULT_BUFFER_SIZE);
        assert(buffer.writerIndex() == 0);
        assert(buffer.readerIndex() == 0);

        boolTest(buffer);
        byteTest(buffer);
        bytesTest(buffer);
        shortTest(buffer);
        intTest(buffer);
        floatTest(buffer);
        doubleTest(buffer);
        stringTest(buffer);
    }
//    void string_utf8_test(ByteBuffer &buffer) {
//        using namespace std;
//        string str = "我爱的人。。。..";
//        wstring utf8_str = buffer.string_to_utf8(str);
//        string str_origin = buffer.utf8_to_string(utf8_str);
//
//        cout << str << endl;
//        cout << str_origin << endl;
//        std::wstring_convert<std::codecvt_utf8<wchar_t>> converter;
//        std::string string = converter.to_bytes(utf8_str);
//        auto list = std::list<char>(string.begin(), string.end());
//        for (auto v : list) {
//            cout << std::to_string(v) << endl;
//        }
//    }

}


#endif
