简体中文 | [English](./README.md)

### Ⅰ. 简介

- [hotswap](https://github.com/zfoo-project/zfoo/blob/main/hotswap/README.md) 热更新代码，不需要停止服务器，不需要额外的任何配置，一行代码开启热更新

### Ⅱ. 单机热更新

- 直接传入需要热更新的class文件
    ```
    // 传入需要更新的class文件
    HotSwapUtils.hotswapClass(bytes);
    ```

### Ⅲ. 分布式热更新

- 将需要热更新的class文件上传到zookeeper注册中心中，其它服务器监听需要更新的class文件，监听到了就去热更新代码

- [tank](https://github.com/zfoo-project/tank-game-server/blob/main/common/src/main/java/com/zfoo/tank/common/util/HotUtils.java)
  分布式热更新代码实现
