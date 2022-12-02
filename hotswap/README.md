English | [简体中文](./README_CN.md)

### Ⅰ. Introduction

- [hotswap](https://github.com/zfoo-project/zfoo/blob/main/hotswap/README.md) Hot update code, no need to stop the
  server, no additional configuration, one line of code to enable hot update

### Ⅱ. Stand-alone hot update

- directly pass in the class file that needs hot update
    ```
    // pass in the class file that needs to be updated
    HotSwapUtils.hotswapClass(bytes);
    ```

### Ⅲ. Distributed Hot Update

- upload the class files that need to be updated to the zookeeper registration center, and other servers monitor the
  class files that need to be updated, and update the code when they are monitored

- [tank](https://github.com/zfoo-project/tank-game-server/blob/main/common/src/main/java/com/zfoo/tank/common/util/HotUtils.java)
  Distributed hot update code implementation
