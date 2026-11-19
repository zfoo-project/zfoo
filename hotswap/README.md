English | [简体中文](./README_CN.md)

### I. Introduction

- [hotswap](https://github.com/zfoo-project/zfoo/blob/main/hotswap/README.md) Hot-swap code without stopping the server or any extra configuration—enable hot-swapping with a single line of code.

### II. Standalone Hot-Swap

- Directly pass in the `.class` file bytes to be hot-swapped:
    ```java
    // Pass in the .class file bytes to be updated
    HotSwapUtils.hotswapClass(bytes);
    ```

### III. Distributed Hot-Swap

- Upload the `.class` files to be updated to the ZooKeeper registry; other servers watch for updated class files and hot-swap the code upon detecting changes.

- [tank](https://github.com/zfoo-project/tank-game-server/blob/main/common/src/main/java/com/zfoo/tank/common/util/HotUtils.java):
  Distributed hot-swap code implementation
