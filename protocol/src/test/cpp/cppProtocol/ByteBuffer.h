#ifndef ZFOO_BYTEBUFFER_H
#define ZFOO_BYTEBUFFER_H

#include <iostream>
#include <string>
#include <vector>
#include <list>
#include <set>
#include <map>
#include <memory>

// 网络传输默认使用大端传输
namespace zfoo {

    using std::string;
    using std::vector;
    using std::list;
    using std::set;
    using std::map;
    using std::make_pair;
    using std::pair;
    using std::unique_ptr;

    // Returns true if the current machine is little endian
    static inline bool is_little_endian() {
        static std::int32_t test = 1;
        return (*reinterpret_cast<std::int8_t *>(&test) == 1);
    }

    // Default size of the buffer
    static const int32_t DEFAULT_BUFFER_SIZE = 2048;
    static const bool IS_LITTLE_ENDIAN = is_little_endian();
    static const string EMPTY_STRING = "";


    class ByteBuffer;

    class IProtocol {
    public:
        virtual int16_t protocolId() = 0;

        virtual ~IProtocol() {
        }
    };

    class IProtocolRegistration {
    public:
        virtual int16_t protocolId() = 0;

        virtual void write(ByteBuffer &buffer, IProtocol *packet) = 0;

        virtual IProtocol *read(ByteBuffer &buffer) = 0;
    };

    IProtocolRegistration *getProtocol(int16_t protocolId);

    class ByteBuffer {
    private:
        int8_t *m_buffer;
        int32_t m_max_capacity;
        int32_t m_writerIndex;
        int32_t m_readerIndex;

    public:
        ByteBuffer(int32_t capacity = DEFAULT_BUFFER_SIZE) : m_max_capacity(capacity) {
            m_buffer = (int8_t *) calloc(m_max_capacity, sizeof(int8_t));
            clear();
        }

        ~ByteBuffer() {
            free(m_buffer);
            m_buffer = nullptr;
        }


        ByteBuffer(const ByteBuffer &buffer) = delete;

        ByteBuffer &operator=(const ByteBuffer &buffer) = delete;


        void clear() {
            m_writerIndex = 0;
            m_readerIndex = 0;
        }

        int32_t writerIndex() const {
            return m_writerIndex;
        }

        int32_t readerIndex() const {
            return m_readerIndex;
        }

        void writerIndex(int32_t writeIndex) {
            if (writeIndex > m_max_capacity) {
                string errorMessage =
                        "writeIndex[" + std::to_string(writeIndex) + "] out of bounds exception: readerIndex: " +
                        std::to_string(m_readerIndex) +
                        ", writerIndex: " + std::to_string(m_writerIndex) +
                        "(expected: 0 <= readerIndex <= writerIndex <= capacity:" + std::to_string(m_max_capacity);
                throw errorMessage;
            }
            m_writerIndex = writeIndex;
        }

        void readerIndex(int32_t readerIndex) {
            if (readerIndex > m_writerIndex) {
                string errorMessage =
                        "readIndex[" + std::to_string(readerIndex) + "] out of bounds exception: readerIndex: " +
                        std::to_string(m_readerIndex) +
                        ", writerIndex: " + std::to_string(m_writerIndex) +
                        "(expected: 0 <= readerIndex <= writerIndex <= capacity:" + std::to_string(m_max_capacity);
                throw errorMessage;
            }
            m_readerIndex = readerIndex;
        }

        inline int32_t getCapacity() const {
            return m_max_capacity - m_writerIndex;
        }

        inline void ensureCapacity(const int32_t &capacity) {
            while (capacity - getCapacity() > 0) {
                int32_t newSize = m_max_capacity * 2;
                int8_t *pBuf = (int8_t *) realloc(m_buffer, newSize);
                if (!pBuf) {
                    std::cout << "relloc failed!" << std::endl;
                    exit(1);
                }
                m_buffer = pBuf;
                m_max_capacity = newSize;
            }
        }

        inline bool isReadable() {
            return m_writerIndex > m_readerIndex;
        }

        inline void writeBool(const bool &value) {
            ensureCapacity(1);
            int8_t v = value ? 1 : 0;
            m_buffer[m_writerIndex++] = v;
        }

        inline bool readBool() {
            int8_t value = m_buffer[m_readerIndex++];
            return value == 1;
        }

        inline void writeByte(const int8_t &value) {
            ensureCapacity(1);
            m_buffer[m_writerIndex++] = value;
        }

        inline int8_t readByte() {
            return m_buffer[m_readerIndex++];
        }

        inline void setByte(const int32_t &index, const int8_t &value) {
            m_buffer[index] = value;
        }

        inline int8_t getByte(const int32_t &index) {
            return m_buffer[index];
        }

        inline void writeBytes(const int8_t *buffer, const int32_t &length) {
            ensureCapacity(length);
            memcpy(&m_buffer[m_writerIndex], buffer, length);
            m_writerIndex += length;
        }

        inline int8_t *readBytes(const int32_t &length) {
            int8_t *bytes = &m_buffer[m_readerIndex];
            m_readerIndex += length;
            return bytes;
        }


        inline void writeShort(const int16_t &value) {
            write(value);
        }

        inline int16_t readShort() {
            return read<int16_t>();
        }

        inline void writeInt(const int32_t &intValue) {
            writeVarInt((uint32_t) ((intValue << 1) ^ (intValue >> 31)));
        }

        inline void writeVarInt(const uint32_t &value) {
            uint32_t a = value >> 7;
            if (a == 0) {
                writeByte((int8_t) value);
                return;
            }

            int32_t writeIndex = m_writerIndex;
            ensureCapacity(5);

            setByte(writeIndex++, (int8_t) (value | 0x80));
            uint32_t b = value >> 14;
            if (b == 0) {
                setByte(writeIndex++, (int8_t) a);
                writerIndex(writeIndex);
                return;
            }

            setByte(writeIndex++, (int8_t) (a | 0x80));
            a = value >> 21;
            if (a == 0) {
                setByte(writeIndex++, (int8_t) b);
                writerIndex(writeIndex);
                return;
            }

            setByte(writeIndex++, (int8_t) (b | 0x80));
            b = value >> 28;
            if (b == 0) {
                setByte(writeIndex++, (int8_t) a);
                writerIndex(writeIndex);
                return;
            }

            setByte(writeIndex++, (int8_t) (a | 0x80));
            setByte(writeIndex++, (int8_t) b);
            writerIndex(writeIndex);
        }

        inline int32_t readInt() {
            int32_t readIndex = m_readerIndex;

            int32_t b = getByte(readIndex++);
            uint32_t value = b;
            if (b < 0) {
                b = getByte(readIndex++);
                value = value & 0x0000007F | b << 7;
                if (b < 0) {
                    b = getByte(readIndex++);
                    value = value & 0x00003FFF | b << 14;
                    if (b < 0) {
                        b = getByte(readIndex++);
                        value = value & 0x001FFFFF | b << 21;
                        if (b < 0) {
                            value = value & 0x0FFFFFFF | getByte(readIndex++) << 28;
                        }
                    }
                }
            }
            readerIndex(readIndex);
            value = ((value >> 1) ^ -((int32_t) value & 1));
            return (int32_t) value;
        }

        inline void writeLong(const int64_t &longValue) {
            uint64_t mask = (uint64_t) ((longValue << 1) ^ (longValue >> 63));

            if (mask >> 32 == 0) {
                writeVarInt((uint32_t) mask);
                return;
            }

            int8_t bytes[9];
            bytes[0] = (int8_t) (mask | 0x80);
            bytes[1] = (int8_t) (mask >> 7 | 0x80);
            bytes[2] = (int8_t) (mask >> 14 | 0x80);
            bytes[3] = (int8_t) (mask >> 21 | 0x80);

            uint32_t a = (uint32_t) (mask >> 28);
            uint32_t b = (uint32_t) (mask >> 35);
            if (b == 0) {
                bytes[4] = (int8_t) a;
                writeBytes(bytes, 5);
                return;
            }

            bytes[4] = (int8_t) (a | 0x80);
            a = (uint32_t) (mask >> 42);
            if (a == 0) {
                bytes[5] = (int8_t) b;
                writeBytes(bytes, 6);
                return;
            }

            bytes[5] = (int8_t) (b | 0x80);
            b = (int) (mask >> 49);
            if (b == 0) {
                bytes[6] = (int8_t) a;
                writeBytes(bytes, 7);
                return;
            }

            bytes[6] = (int8_t) (a | 0x80);
            a = (int) (mask >> 56);
            if (a == 0) {
                bytes[7] = (int8_t) b;
                writeBytes(bytes, 8);
                return;
            }

            bytes[7] = (int8_t) (b | 0x80);
            bytes[8] = (int8_t) a;
            writeBytes(bytes, 9);
        }

        inline int64_t readLong() {
            int32_t readIndex = m_readerIndex;

            int64_t b = getByte(readIndex++);
            uint64_t value = b;
            if (b < 0) {
                b = getByte(readIndex++);
                value = value & 0x000000000000007FLL | b << 7;
                if (b < 0) {
                    b = getByte(readIndex++);
                    value = value & 0x0000000000003FFFLL | b << 14;
                    if (b < 0) {
                        b = getByte(readIndex++);
                        value = value & 0x00000000001FFFFFLL | b << 21;
                        if (b < 0) {
                            b = getByte(readIndex++);
                            value = value & 0x000000000FFFFFFFLL | b << 28;
                            if (b < 0) {
                                b = getByte(readIndex++);
                                value = value & 0x00000007FFFFFFFFLL | b << 35;
                                if (b < 0) {
                                    b = getByte(readIndex++);
                                    value = value & 0x000003FFFFFFFFFFLL | b << 42;
                                    if (b < 0) {
                                        b = getByte(readIndex++);
                                        value = value & 0x0001FFFFFFFFFFFFLL | b << 49;
                                        if (b < 0) {
                                            b = getByte(readIndex++);
                                            value = value & 0x00FFFFFFFFFFFFFFLL | b << 56;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            readerIndex(readIndex);
            value = ((value >> 1) ^ -((int64_t) value & 1));
            return (int64_t) value;
        }

        inline void writeFloat(const float &value) {
            write(value);
        }

        inline float readFloat() {
            return read<float>();
        }

        inline void writeDouble(const double &value) {
            write(value);
        }

        inline double readDouble() {
            return read<double>();
        }

        inline void writeString(const string &value) {
            if (value.empty()) {
                writeInt(0);
                return;
            }
            int32_t length = value.size() * sizeof(value.front());
            writeInt(length);
            writeBytes(reinterpret_cast<const int8_t *>(&value[0]), length);
        }

        inline string readString() {
            int32_t length = readInt();
            if (length <= 0) {
                return EMPTY_STRING;
            }

            auto bytes = readBytes(length);
            string str(reinterpret_cast<const char *>(bytes), length);
            return str;
        }

        // 很多脚本语言没有char，所以这里使用string代替
        inline void writeChar(const char &value) {
            string str;
            str.push_back(value);
            writeString(str);
        }

        inline char readChar() {
            return readString()[0];
        }

        inline bool writePacketFlag(const IProtocol *packet) {
            bool flag = packet == nullptr;
            writeBool(!flag);
            return flag;
        }

        inline void writePacket(IProtocol *packet, const int16_t &protocolId) {
            IProtocolRegistration *protocolRegistration = getProtocol(protocolId);
            protocolRegistration->write(*this, packet);
        }

        inline unique_ptr<IProtocol> readPacket(const int16_t &protocolId) {
            IProtocolRegistration *protocolRegistration = getProtocol(protocolId);
            auto packet = protocolRegistration->read(*this);
            return unique_ptr<IProtocol>(packet);
        }


        //---------------------------------boolean--------------------------------------
        inline void writeBooleanArray(const vector<bool> &array) {
            if (array.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = array.size();
            writeInt(length);
            for (auto value : array) {
                writeBool(value);
            }
        }

        inline vector<bool> readBooleanArray() {
            int32_t length = readInt();
            int8_t *bytes = readBytes(length);
            vector<bool> array;
            for (auto i = 0; i < length; i++) {
                array.emplace_back((bytes[i] == 1));
            }
            return array;
        }

        inline void writeBooleanList(const list<bool> &list) {
            if (list.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = list.size();
            writeInt(length);
            for (auto value : list) {
                writeBool(value);
            }
        }

        inline list<bool> readBooleanList() {
            int32_t length = readInt();
            list<bool> list;
            for (auto i = 0; i < length; i++) {
                list.emplace_back(readBool());
            }
            return list;
        }

        inline void writeBooleanSet(const set<bool> &set) {
            if (set.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = set.size();
            writeInt(length);
            for (auto value : set) {
                writeBool(value);
            }
        }

        inline set<bool> readBooleanSet() {
            int32_t length = readInt();
            set<bool> set;
            for (auto i = 0; i < length; i++) {
                set.emplace(readBool());
            }
            return set;
        }

        //---------------------------------byte--------------------------------------
        inline void writeByteArray(const vector<int8_t> &array) {
            if (array.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = array.size();
            writeInt(length);
            for (auto value : array) {
                writeByte(value);
            }
        }

        inline vector<int8_t> readByteArray() {
            int32_t length = readInt();
            int8_t *bytes = readBytes(length);
            vector<int8_t> array;
            for (auto i = 0; i < length; i++) {
                array.emplace_back(bytes[i]);
            }
            return array;
        }

        inline void writeByteList(const list<int8_t> &list) {
            if (list.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = list.size();
            writeInt(length);
            for (auto value : list) {
                writeByte(value);
            }
        }

        inline list<int8_t> readByteList() {
            int32_t length = readInt();
            list<int8_t> list;
            for (auto i = 0; i < length; i++) {
                list.emplace_back(readByte());
            }
            return list;
        }

        inline void writeByteSet(const set<int8_t> &set) {
            if (set.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = set.size();
            writeInt(length);
            for (auto value : set) {
                writeByte(value);
            }
        }

        inline set<int8_t> readByteSet() {
            int32_t length = readInt();
            set<int8_t> set;
            for (auto i = 0; i < length; i++) {
                set.emplace(readByte());
            }
            return set;
        }

        //---------------------------------short--------------------------------------
        inline void writeShortArray(const vector<int16_t> &array) {
            if (array.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = array.size();
            writeInt(length);
            for (auto value : array) {
                writeShort(value);
            }
        }

        inline vector<int16_t> readShortArray() {
            int32_t length = readInt();
            vector<int16_t> array;
            for (auto i = 0; i < length; i++) {
                array.emplace_back(readShort());
            }
            return array;
        }

        inline void writeShortList(const list<int16_t> &list) {
            if (list.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = list.size();
            writeInt(length);
            for (auto value : list) {
                writeShort(value);
            }
        }

        inline list<int16_t> readShortList() {
            int32_t length = readInt();
            list<int16_t> list;
            for (auto i = 0; i < length; i++) {
                list.emplace_back(readShort());
            }
            return list;
        }

        inline void writeShortSet(const set<int16_t> &set) {
            if (set.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = set.size();
            writeInt(length);
            for (auto value : set) {
                writeShort(value);
            }
        }

        inline set<int16_t> readShortSet() {
            int32_t length = readInt();
            set<int16_t> set;
            for (auto i = 0; i < length; i++) {
                set.emplace(readShort());
            }
            return set;
        }

        //---------------------------------int--------------------------------------
        inline void writeIntArray(const vector<int32_t> &array) {
            if (array.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = array.size();
            writeInt(length);
            for (auto value : array) {
                writeInt(value);
            }
        }

        inline vector<int32_t> readIntArray() {
            int32_t length = readInt();
            vector<int32_t> array;
            for (auto i = 0; i < length; i++) {
                array.emplace_back(readInt());
            }
            return array;
        }

        inline void writeIntList(const list<int32_t> &list) {
            if (list.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = list.size();
            writeInt(length);
            for (auto value : list) {
                writeInt(value);
            }
        }

        inline list<int32_t> readIntList() {
            int32_t length = readInt();
            list<int32_t> list;
            for (auto i = 0; i < length; i++) {
                list.emplace_back(readInt());
            }
            return list;
        }

        inline void writeIntSet(const set<int32_t> &set) {
            if (set.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = set.size();
            writeInt(length);
            for (auto value : set) {
                writeInt(value);
            }
        }

        inline set<int32_t> readIntSet() {
            int32_t length = readInt();
            set<int32_t> set;
            for (auto i = 0; i < length; i++) {
                set.emplace(readInt());
            }
            return set;
        }

        //---------------------------------long--------------------------------------
        inline void writeLongArray(const vector<int64_t> &array) {
            if (array.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = array.size();
            writeInt(length);
            for (auto value : array) {
                writeLong(value);
            }
        }

        inline vector<int64_t> readLongArray() {
            int32_t length = readInt();
            vector<int64_t> array;
            for (auto i = 0; i < length; i++) {
                array.emplace_back(readLong());
            }
            return array;
        }

        inline void writeLongList(const list<int64_t> &list) {
            if (list.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = list.size();
            writeInt(length);
            for (auto value : list) {
                writeLong(value);
            }
        }

        inline list<int64_t> readLongList() {
            int32_t length = readInt();
            list<int64_t> list;
            for (auto i = 0; i < length; i++) {
                list.emplace_back(readLong());
            }
            return list;
        }

        inline void writeLongSet(const set<int64_t> &set) {
            if (set.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = set.size();
            writeInt(length);
            for (auto value : set) {
                writeLong(value);
            }
        }

        inline set<int64_t> readLongSet() {
            int32_t length = readInt();
            set<int64_t> set;
            for (auto i = 0; i < length; i++) {
                set.emplace(readLong());
            }
            return set;
        }

        //---------------------------------float--------------------------------------
        inline void writeFloatArray(const vector<float> &array) {
            if (array.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = array.size();
            writeInt(length);
            for (auto value : array) {
                writeFloat(value);
            }
        }

        inline vector<float> readFloatArray() {
            int32_t length = readInt();
            vector<float> array;
            for (auto i = 0; i < length; i++) {
                array.emplace_back(readFloat());
            }
            return array;
        }

        inline void writeFloatList(const list<float> &list) {
            if (list.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = list.size();
            writeInt(length);
            for (auto value : list) {
                writeFloat(value);
            }
        }

        inline list<float> readFloatList() {
            int32_t length = readInt();
            list<float> list;
            for (auto i = 0; i < length; i++) {
                list.emplace_back(readFloat());
            }
            return list;
        }

        inline void writeFloatSet(const set<float> &list) {
            if (list.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = list.size();
            writeInt(length);
            for (auto value : list) {
                writeFloat(value);
            }
        }

        inline set<float> readFloatSet() {
            int32_t length = readInt();
            set<float> set;
            for (auto i = 0; i < length; i++) {
                set.emplace(readFloat());
            }
            return set;
        }

        //---------------------------------double--------------------------------------
        inline void writeDoubleArray(const vector<double> &array) {
            if (array.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = array.size();
            writeInt(length);
            for (auto value : array) {
                writeDouble(value);
            }
        }

        inline vector<double> readDoubleArray() {
            int32_t length = readInt();
            vector<double> array;
            for (auto i = 0; i < length; i++) {
                array.emplace_back(readDouble());
            }
            return array;
        }

        inline void writeDoubleList(const list<double> &list) {
            if (list.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = list.size();
            writeInt(length);
            for (auto value : list) {
                writeDouble(value);
            }
        }

        inline list<double> readDoubleList() {
            int32_t length = readInt();
            list<double> list;
            for (auto i = 0; i < length; i++) {
                list.emplace_back(readDouble());
            }
            return list;
        }

        inline void writeDoubleSet(const set<double> &set) {
            if (set.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = set.size();
            writeInt(length);
            for (auto value : set) {
                writeDouble(value);
            }
        }

        inline set<double> readDoubleSet() {
            int32_t length = readInt();
            set<double> set;
            for (auto i = 0; i < length; i++) {
                set.emplace(readDouble());
            }
            return set;
        }

        //---------------------------------char--------------------------------------
        inline void writeCharArray(const vector<char> &array) {
            if (array.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = array.size();
            writeInt(length);
            for (auto value : array) {
                writeChar(value);
            }
        }

        inline vector<char> readCharArray() {
            int32_t length = readInt();
            vector<char> array;
            for (auto i = 0; i < length; i++) {
                array.emplace_back(readChar());
            }
            return array;
        }

        inline void writeCharList(const list<char> &list) {
            if (list.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = list.size();
            writeInt(length);
            for (auto value : list) {
                writeChar(value);
            }
        }

        inline list<char> readCharList() {
            int32_t length = readInt();
            list<char> list;
            for (auto i = 0; i < length; i++) {
                list.emplace_back(readChar());
            }
            return list;
        }

        inline void writeCharSet(const set<char> &set) {
            if (set.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = set.size();
            writeInt(length);
            for (auto value : set) {
                writeChar(value);
            }
        }

        inline set<char> readCharSet() {
            int32_t length = readInt();
            set<char> set;
            for (auto i = 0; i < length; i++) {
                set.emplace(readChar());
            }
            return set;
        }

        //---------------------------------string--------------------------------------
        inline void writeStringArray(const vector<string> &array) {
            if (array.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = array.size();
            writeInt(length);
            for (auto value : array) {
                writeString(value);
            }
        }

        inline vector<string> readStringArray() {
            int32_t length = readInt();
            vector<string> array;
            for (auto i = 0; i < length; i++) {
                array.emplace_back(readString());
            }
            return array;
        }

        inline void writeStringList(const list<string> &list) {
            if (list.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = list.size();
            writeInt(length);
            for (auto value : list) {
                writeString(value);
            }
        }

        inline list<string> readStringList() {
            int32_t length = readInt();
            list<string> list;
            for (auto i = 0; i < length; i++) {
                list.emplace_back(readString());
            }
            return list;
        }

        inline void writeStringSet(const set<string> &set) {
            if (set.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = set.size();
            writeInt(length);
            for (auto value : set) {
                writeString(value);
            }
        }

        inline set<string> readStringSet() {
            int32_t length = readInt();
            set<string> set;
            for (auto i = 0; i < length; i++) {
                set.emplace(readString());
            }
            return set;
        }

        inline void writeIntIntMap(const map<int32_t, int32_t> &map) {
            if (map.empty()) {
                writeByte(0);
                return;
            }
            writeInt(map.size());
            for (const auto&[key, value] : map) {
                writeInt(key);
                writeInt(value);
            }
        }

        inline map<int32_t, int32_t> readIntIntMap() {
            int32_t length = readInt();
            map<int32_t, int32_t> map;
            for (auto i = 0; i < length; i++) {
                auto key = readInt();
                auto value = readInt();
                map.insert(pair<int32_t, int32_t>(key, value));
            }
            return map;
        }

        inline void writeIntLongMap(const map<int32_t, int64_t> &map) {
            if (map.empty()) {
                writeByte(0);
                return;
            }
            writeInt(map.size());
            for (const auto&[key, value] : map) {
                writeInt(key);
                writeLong(value);
            }
        }

        inline map<int32_t, int64_t> readIntLongMap() {
            int32_t length = readInt();
            map<int32_t, int64_t> map;
            for (auto i = 0; i < length; i++) {
                auto key = readInt();
                auto value = readLong();
                map.insert(pair<int32_t, int64_t>(key, value));
            }
            return map;
        }

        inline void writeIntStringMap(const map<int32_t, string> &map) {
            if (map.empty()) {
                writeByte(0);
                return;
            }
            writeInt(map.size());
            for (const auto&[key, value] : map) {
                writeInt(key);
                writeString(value);
            }
        }

        inline map<int32_t, string> readIntStringMap() {
            int32_t length = readInt();
            map<int32_t, string> map;
            for (auto i = 0; i < length; i++) {
                auto key = readInt();
                auto value = readString();
                map.insert(pair<int32_t, string>(key, value));
            }
            return map;
        }

        template<class T>
        inline void writeIntPacketMap(const map<int32_t, T> &map, const int16_t &protocolId) {
            if (map.empty()) {
                writeByte(0);
                return;
            }
            writeInt(map.size());
            for (const auto&[key, value] : map) {
                writeInt(key);
                writePacket((IProtocol *) &value, protocolId);
            }
        }

        template<class T>
        inline map<int32_t, T> readIntPacketMap(const int16_t &protocolId) {
            int32_t length = readInt();
            map<int32_t, T> map;
            for (auto i = 0; i < length; i++) {
                auto key = readInt();
                auto value = readPacket(protocolId);
                auto *p = (T *) value.get();
                map.insert(pair<int32_t, T>(key, *p));
            }
            return map;
        }

        inline void writeLongIntMap(const map<int64_t, int32_t> &map) {
            if (map.empty()) {
                writeByte(0);
                return;
            }
            writeInt(map.size());
            for (const auto&[key, value] : map) {
                writeLong(key);
                writeInt(value);
            }
        }

        inline map<int64_t, int32_t> readLongIntMap() {
            int32_t length = readInt();
            map<int64_t, int32_t> map;
            for (auto i = 0; i < length; i++) {
                auto key = readLong();
                auto value = readInt();
                map.insert(pair<int64_t, int32_t>(key, value));
            }
            return map;
        }

        inline void writeLongLongMap(const map<int64_t, int64_t> &map) {
            if (map.empty()) {
                writeByte(0);
                return;
            }
            writeInt(map.size());
            for (const auto&[key, value] : map) {
                writeLong(key);
                writeLong(value);
            }
        }

        inline map<int64_t, int64_t> readLongLongMap() {
            int32_t length = readInt();
            map<int64_t, int64_t> map;
            for (auto i = 0; i < length; i++) {
                auto key = readLong();
                auto value = readLong();
                map.insert(pair<int64_t, int64_t>(key, value));
            }
            return map;
        }

        inline void writeLongStringMap(const map<int64_t, string> &map) {
            if (map.empty()) {
                writeByte(0);
                return;
            }
            writeInt(map.size());
            for (const auto&[key, value] : map) {
                writeLong(key);
                writeString(value);
            }
        }

        inline map<int64_t, string> readLongStringMap() {
            int32_t length = readInt();
            map<int64_t, string> map;
            for (auto i = 0; i < length; i++) {
                auto key = readLong();
                auto value = readString();
                map.insert(pair<int64_t, string>(key, value));
            }
            return map;
        }

        template<class T>
        inline void writeLongPacketMap(const map<int64_t, T> &map, const int16_t &protocolId) {
            if (map.empty()) {
                writeByte(0);
                return;
            }
            writeInt(map.size());
            for (const auto&[key, value] : map) {
                writeLong(key);
                writePacket((IProtocol *) &value, protocolId);
            }
        }

        template<class T>
        inline map<int64_t, T> readLongPacketMap(const int16_t &protocolId) {
            int32_t length = readInt();
            map<int64_t, T> map;
            for (auto i = 0; i < length; i++) {
                auto key = readLong();
                auto value = readPacket(protocolId);
                auto *p = (T *) value.get();
                map.insert(pair<int64_t, T>(key, *p));
            }
            return map;
        }

        inline void writeStringIntMap(const map<string, int32_t> &map) {
            if (map.empty()) {
                writeByte(0);
                return;
            }
            writeInt(map.size());
            for (const auto&[key, value] : map) {
                writeString(key);
                writeInt(value);
            }
        }

        inline map<string, int32_t> readStringIntMap() {
            int32_t length = readInt();
            map<string, int32_t> map;
            for (auto i = 0; i < length; i++) {
                auto key = readString();
                auto value = readInt();
                map.insert(pair<string, int32_t>(key, value));
            }
            return map;
        }

        inline void writeStringLongMap(const map<string, int64_t> &map) {
            if (map.empty()) {
                writeByte(0);
                return;
            }
            writeInt(map.size());
            for (const auto&[key, value] : map) {
                writeString(key);
                writeLong(value);
            }
        }

        inline map<string, int64_t> readStringLongMap() {
            int32_t length = readInt();
            map<string, int64_t> map;
            for (auto i = 0; i < length; i++) {
                auto key = readString();
                auto value = readLong();
                map.insert(pair<string, int64_t>(key, value));
            }
            return map;
        }

        inline void writeStringStringMap(const map<string, string> &map) {
            if (map.empty()) {
                writeByte(0);
                return;
            }
            writeInt(map.size());
            for (const auto&[key, value] : map) {
                writeString(key);
                writeString(value);
            }
        }

        inline map<string, string> readStringStringMap() {
            int32_t length = readInt();
            map<string, string> map;
            for (auto i = 0; i < length; i++) {
                auto key = readString();
                auto value = readString();
                map.insert(pair<string, string>(key, value));
            }
            return map;
        }

        template<class T>
        inline void writeStringPacketMap(const map<string, T> &map, const int16_t &protocolId) {
            if (map.empty()) {
                writeByte(0);
                return;
            }
            writeInt(map.size());
            for (const auto&[key, value] : map) {
                writeString(key);
                writePacket((IProtocol *) &value, protocolId);
            }
        }

        template<class T>
        inline map<string, T> readStringPacketMap(const int16_t &protocolId) {
            int32_t length = readInt();
            map<string, T> map;
            for (auto i = 0; i < length; i++) {
                auto key = readString();
                auto value = readPacket(protocolId);
                auto *p = (T *) value.get();
                map.insert(pair<string, T>(key, *p));
            }
            return map;
        }


        //---------------------------------packet--------------------------------------
        template<class T>
        inline void writePacketArray(const vector<T> &array, const int16_t &protocolId) {
            if (array.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = array.size();
            writeInt(length);
            for (auto value : array) {
                writePacket((IProtocol *) &value, protocolId);
            }
        }

        template<class T>
        inline vector<T> readPacketArray(const int16_t &protocolId) {
            int32_t length = readInt();
            vector<T> array;
            for (auto i = 0; i < length; i++) {
                auto value = readPacket(protocolId);
                auto *p = (T *) value.get();
                array.emplace_back(*p);
            }
            return array;
        }

        template<class T>
        inline void writePacketList(const list<T> &list, const int16_t &protocolId) {
            if (list.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = list.size();
            writeInt(length);
            for (auto value : list) {
                writePacket((IProtocol *) &value, protocolId);
            }
        }

        template<class T>
        inline list<T> readPacketList(const int16_t &protocolId) {
            int32_t length = readInt();
            list<T> list;
            for (auto i = 0; i < length; i++) {
                auto value = readPacket(protocolId);
                auto *p = (T *) value.get();
                list.emplace_back(*p);
            }
            return list;
        }

        template<class T>
        inline void writePacketSet(const set<T> &set, const int16_t &protocolId) {
            if (set.empty()) {
                writeByte(0);
                return;
            }
            int32_t length = set.size();
            writeInt(length);
            for (auto value : set) {
                writePacket((IProtocol *) &value, protocolId);
            }
        }

        template<class T>
        inline set<T> readPacketSet(const int16_t &protocolId) {
            int32_t length = readInt();
            set<T> set;
            for (auto i = 0; i < length; i++) {
                auto value = readPacket(protocolId);
                auto *p = (T *) value.get();
                set.emplace(*p);
            }
            return set;
        }

    private:
        template<class T>
        inline void write(T value) {
            ensureCapacity(sizeof(T));
            // MSDN: The htons function converts a u_short from host to TCP/IP network byte order (which is big-endian).
            // ** This mean the network byte order is big-endian **
            if (IS_LITTLE_ENDIAN) {
                swap_bytes<sizeof(T)>(reinterpret_cast<std::int8_t *>(&value));
            }
            memcpy(&m_buffer[m_writerIndex], (int8_t *) &value, sizeof(T));
            m_writerIndex += sizeof(T);
        }

        template<class T>
        inline T read() {
            T value = *((T *) &m_buffer[m_readerIndex]);
            if (IS_LITTLE_ENDIAN) {
                swap_bytes<sizeof(T)>(reinterpret_cast<int8_t *>(&value));
            }
            m_readerIndex += sizeof(T);
            return value;
        }

        // Swaps the order of bytes for some chunk of memory
        template<std::size_t DataSize>
        inline void swap_bytes(int8_t *data) {
            for (std::size_t i = 0, end = DataSize / 2; i < end; ++i) {
                std::swap(data[i], data[DataSize - i - 1]);
            }
        }
    };

}

#endif
