#ifndef ZFOO_LIST_TEST_H
#define ZFOO_LIST_TEST_H

#include <cstdint>
#include "list.h"

namespace list_test {
    void assert(bool flag, std::string errorMessage) {
        if (!flag) {
            throw errorMessage;
        }
    }

    void listCopyTest() {
        using namespace zfoo;
        using namespace std;
        List<int64_t> list;
        int64_t a = 1;
        int64_t b = 2;
        int64_t c = 3;
        list.add(a);
        list.add(b);
        list.add(c);

        List<int64_t> newList = list;
        assert(newList.size() == 3, "listCopyTest size exception");
        assert(*newList.get(2) == 3, "listCopyTest get exception");
        assert(newList.indexOf(c) == 2, "listCopyTest indexOf exception");
    }

    void listOperatorTest() {
        using namespace zfoo;
        using namespace std;
        List<int64_t> list;
        int64_t a = 1;
        int64_t b = 2;
        int64_t c = 3;
        list.add(a);
        list.add(b);
        list.add(c);

        List<int64_t> newList;
        newList = list;
        assert(newList.size() == 3, "listOperatorTest size exception");
        assert(*newList.get(2) == 3, "listOperatorTest get exception");
        assert(newList.indexOf(c) == 2, "listOperatorTest indexOf exception");
        newList = newList;
        newList = newList;
    }

    void listIntTest() {
        using namespace zfoo;
        using namespace std;
        List<int> list;
        assert(list.isEmpty(), "listIntTest isEmpty exception");
        int a = 1;
        int b = 2;
        int c = 3;
        list.add(a);
        list.add(b);
        list.add(c);
        assert(list.size() == 3, "listIntTest size exception");
        assert(*list.get(1) == 2, "listIntTest get exception");
        assert(list.indexOf(b) == 1, "listIntTest indexOf exception");
    }

    void listRemoveTest() {
        using namespace zfoo;
        using namespace std;
        using namespace zfoo;
        using namespace std;
        List<int64_t> list;
        int64_t a = 1;
        int64_t b = 2;
        int64_t c = 3;
        int64_t d = 4;
        int64_t e = 5;
        int64_t f = 6;
        int64_t g = 7;
        list.add(a);
        list.add(b);
        list.add(c);
        list.add(d);
        list.add(e);
        list.add(f);

        List<int64_t> list1 = list;
        List<int64_t> list2 = list;

        assert(!list.remove(g), "listRemoveTest remove exception");
        assert(list.remove(f), "listRemoveTest remove exception");
        assert(*list.get(0) == a, "listRemoveTest get exception");
        assert(*list.get(4) == e, "listRemoveTest get exception");

        list1.remove(a);
        assert(list1.get(5) == nullptr, "listRemoveTest get exception");
        assert(*list1.get(0) == b, "listRemoveTest get exception");
        assert(*list1.get(4) == f, "listRemoveTest get exception");

        list2.remove(c);
        assert(*list2.get(0) == a, "listRemoveTest get exception");
        assert(*list2.get(4) == f, "listRemoveTest get exception");
    }


    void list_all_test() {
        using namespace std;
        try {
            listCopyTest();
            cout << "----------------------------------------------------------------------" << endl;
            listOperatorTest();
            cout << "----------------------------------------------------------------------" << endl;
            listIntTest();
            cout << "----------------------------------------------------------------------" << endl;
            listRemoveTest();
            cout << "----------------------------------------------------------------------" << endl;
        } catch (string &e) {
            cout << e << endl;
        } catch (...) {
            cout << "unknown" << endl;
        }
    }
}
#endif
