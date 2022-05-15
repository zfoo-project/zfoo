#ifndef ZFOO_LIST_H
#define ZFOO_LIST_H

#include <iostream>

namespace zfoo {

    template<class T>
    class List {
    private:
        int max_list_size;
        int length;
        T *head;

    private:
        void out() {
            T *p = head;
            for (int i = 0; i < length; i++, p++) {
                std::cout << *p << "  ";       //setw(sizeof(int))
            }
        }

    public:
        List() {
            //默认构造函数，创建大小为100的空间
            max_list_size = 10;
            head = new T[max_list_size];
            length = 0;
        }

        List(const List<T> &list) {
            max_list_size = list.max_list_size;
            length = list.length;
            head = new T[max_list_size];
            memcpy(head, list.head, max_list_size * sizeof(T));
            std::cout << "copy" << std::endl;
        }

        List &operator=(const List<T> &list) {
            if (this == &list) {
                return *this;
            }
            if (head != nullptr) {
                delete[] head;
            }
            max_list_size = list.max_list_size;
            length = list.length;
            head = new T[max_list_size];
            memcpy(head, list.head, max_list_size * sizeof(T));
            std::cout << "operator" << std::endl;
            return *this;
        }


        ~List() {
            // 程序完成后，自动调用，释放数组空间
            if (head != nullptr) {
                delete[] head;
                std::cout << "deconstructor" << std::endl;
            }
        }

        bool isEmpty() const { return length == 0; };     //判断线性表是否为空
        int size() const { return length; };              //返回线性表长度

        void add(T &x) {
            if (length >= max_list_size) {
                int newSize = max_list_size * 2;
                T *p = (T *) realloc(head, newSize * sizeof(T));
                max_list_size = newSize;
                head = p;
            }
            head[length++] = x;
        }

        T *get(int index) { return (index >= length) ? nullptr : &head[index]; }

        int indexOf(T &x) {// return -1 if x not in list.
            // return -1 if x not in list.
            for (int i = 0; i < length; i++) {
                if (head[i] == x) {
                    return i;
                }
            }
            return -1;
        }

        bool remove(T &x) {
            int index = indexOf(x);
            if (index < 0) {
                return false;
            }
            // 开始移除
            length--;
            if (index == length) {
                return true;
            }
            memmove(&head[index], &head[index + 1], (length - index) * sizeof(T));
            return true;
        }


    public:
        friend std::ostream &operator<<(std::ostream &cout, List<T> &list) {
            list.out();
            return cout;
        }

        bool operator<(const List &rhs) const {
            if (max_list_size < rhs.max_list_size)
                return true;
            if (rhs.max_list_size < max_list_size)
                return false;
            if (length < rhs.length)
                return true;
            if (rhs.length < length)
                return false;
            return head < rhs.head;
        }

        bool operator>(const List &rhs) const {
            return rhs < *this;
        }

        bool operator<=(const List &rhs) const {
            return !(rhs < *this);
        }

        bool operator>=(const List &rhs) const {
            return !(*this < rhs);
        }
    };
}

#endif
