# 一、netty模型

## 1.reactor线程驱动模型

```
bossGroup 线程池则只是在 Bind 某个端口后，获得其中一个线程作为 MainReactor，
    专门处理端口的 Accept 事件，每个端口对应一个 Boss 线程。
workerGroup 线程池会被各个 SubReactor 和 Worker 线程充分利用。

```

## 2.Netty事件模型

```
核心思路基于主从Reactor模型，一个NioEventLoopGroup包含n个NioEventLoop,
每一个NioEventLoop持有一个Selector和一个线程池（执行器EventExecute,其实是netty ForkJoinPool,并发度为n),
在选择器的NioEventLoop的run方法每次运行后，就会交给NioEventLoop中线程池的另外一个线程。
```

## 3.I/O复用模型

- Netty 的非阻塞 I/O 的实现关键是基于 I/O 复用模型，这里用 Selector 对象表示，基于Java的NIO，netty会有多个Selector

```
DK的NIO类库有一个epoll死循环bug，它会导致Selector空轮询，IO线程CPU达到100%，严重影响系统运行。
Netty的解决策略：
1.对Selector的select操作周期进行统计。
2.每完成一次空的select操作进行一次计数。
3.在某个周期内如果连续N次空轮询，则说明触发了JDK NIO的epoll死循环bug。
4.创建新的Selector，将出现bug的Selector上的channel重新注册到新的Selector上。
5.关闭bug的Selector，使用新的Selector进行替换。
```

# 二、netty设置

## 1.netty有哪些参数设置

    1.TCP_NODELAY
        解释：是否启用Nagle算法，改算法将小的碎片数据连接成更大的报文来最小化所发送的报文的数量。 
        使用建议：如果需要发送一些较小的报文，则需要禁用该算法，从而最小化报文传输延时。
        只有在网络通信非常大时（通常指已经到100k+/秒了），设置为false会有些许优势，因此建议大部分情况下均应设置为true。
        
    2.SO_REUSEADDR
        解释：是否复用处于TIME_WAIT状态连接的端口，适用于有大量处于TIME_WAIT状态连接的场景，如高并发量的Http短连接场景等。

    3.SO_SNDBUF
        解释：Socket参数，TCP数据发送缓冲区大小，即TCP发送滑动窗口，linux操作系统可使用命令：cat /proc/sys/net/ipv4/tcp_smem查询其大小。
        缓冲区的大小决定了网络通信的吞吐量（网络吞吐量=缓冲区大小/网络时延）。 
        使用建议：缓冲区大小设为网络吞吐量达到带宽上限时的值，即缓冲区大小=网络带宽*网络时延。以千兆网卡为例进行计算，假设网络时延为1ms，缓冲区大小=1000Mb/s * 1ms = 128KB。

    4.SO_KEEPALIVE
    　　解释：是否使用TCP的心跳机制； 
    　　使用建议：心跳机制由应用层自己实现；

## 2.netty最佳的Boss线程连接数

```
没有固定的连接数，一切还是看你的场景，连接数在满足传输吞吐量的情况下，越少越好。

2条连接时，只能有40k QPS。

48条连接，升到62k QPS，CPU烧了28%

4条连接，QPS反而上升到68k ，而且CPU降到20%。
```

## 3.netty线程池怎么设置

```
Boss Group用于服务端处理建立连接的请求，WorkGroup用于处理I/O。
EventLoopGroup的默认大小都是是2倍的CPU核数，但这并不是一个恒定的最佳数量，为了避免线程上下文切换，只要能满足要求，这个值其实越少越好。
如果都是长连接，Boss Group平时很闲，好在它也只有忙起来才会多起线程，平时就只占1条。


Netty线程的数量一般固定且较少，所以很怕线程被堵塞，比如同步的数据库查询，
比如下游的服务调用（又来罗嗦，future.get()式的异步在执行future.get()时还是堵住当前线程的啊）。
所以，此时就要把处理放到一个业务线程池里操作，即使要付出线程上下文切换的代价
```

## 4.netty的JVM参数设置

```
-Dio.netty.leakDetectionLevel=disabled把检测关掉
堆外内存大小
```

## 5.netty注意事项

```
1. ctx.writeAndFlush() 与 channel.writeAndFlush()的区别在于，channel要经过整条Pipeline，而ctx直接找下一个outboundHandler。

2. channel.writeAndFlush(buf, channel.voidPromise() )
writeAndFlush不管你用不用默认构造返回一个Promise(Future)，有点浪费内存。没有用的话，用一个公共的 voidPromise ，减少大家花费。不过低版本的Netty不能用。

3. 空闲连接管理，因为刚才说的ctx.writeAndFlush()不经Pipeline，所以只监控读空闲就够了。否则每次请求都要READ/WRITE/ALL IDEL三个值算一遍，白白消耗。

4. Handler能共用就标上Shareable Annotation然后共用，不要每个Channel建一个。
```
