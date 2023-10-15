#include "test/list_test.h"
#include "test/byte_buffer_test.h"
#include "test/serialization_test.h"
#include "test/speed_test.h"

using namespace zfoo;
using namespace std;

int main() {
    list_test::list_all_test();
    byte_buffer_test::byte_buffer_all_test();
    serialization_test::protocol_all_test();

    speed_test::parseObject();
    speed_test::singleThreadBenchmarks();
    return 0;
}