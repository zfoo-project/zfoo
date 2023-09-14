English | [简体中文](./README_CN.md)

### Ⅰ. Introduction

- [net](https://github.com/zfoo-project/zfoo/blob/main/net/README.md) Asynchronous, actor design,
  lock-free, universal RPC framework based on Spring's MVC-style usage

1. Elegant synchronous and asynchronous requests, faster, supports tcp udp websocket
2. Service registration and discovery, the configuration center uses zookeeper, which can be extended to other
   registration centers
3. Built-in high-performance gateway, custom forwarding strategy
4. Scalable services, load balancing, cluster monitoring, everything you expect

### Ⅱ. Related modules

- net provides two layers of api interface
  - IRouter(low level api) The underlying network interface, which sends messages directly through the Session
    connection
  - IConsumer(high level api) The high-level network interface is the encapsulation of I Router, and the consumer sends
    RPC messages to the service provider

```
Extreme high performance, high availability, high scalability (generally refers to adding machines), 
high scalability (generally refers to the principle of opening and closing at the code level)

config，local configuration, zookeeper registration discovery, and request load balancing are all placed under this package
core，unified package of core package, server and client
router，message routing
handler，netty's handler defines some common handlers for clients and servers
protocol，registration of message classes, encoding and decoding of messages, bytecode enhancement, etc.
schema，analysis of spring's custom tags
session，encapsulation of netty's channel
task，task thread pool, task distribution
```

- Network communication specification
  - The client's request to the server ends with Request, and the return ends with Response
  - The call between the servers ends with Ask, and the return ends with Answer.

#### Background

```
When using Java to make online games involves more complex interactive scenarios, there are a lot of remote requests.
At this time, a high-performance rpc framework is needed.

There are many rpc frameworks in the Java ecosystem, but few rpc frameworks can fit into the rpc framework of game projects. 
Some of them are listed below：
    dubbo，The code is heavyweight, asynchronous calls are cumbersome, there is no gateway, and it is impossible to directly interact with the client;
    grpc，The code is heavyweight, complex to use, complex to deploy, no gateway, difficult to customize;
    akka，Using Scala language, the learning cost is extremely high, just give up;
    
So I wrote a universal rpc framework, the code is lightweight, can be easily customized, with its own gateway, inherently asynchronous, and the tool chain is complete.
After subsequent continuous optimization, a serialization framework with the very fast speed in Java was also made, and then the speed took off directly.
Later, I found out that it can be used not only for games, but also for rpc scenes with extreme performance requirements.
```

### Ⅱ. Server Architecture Diagram

![Image text](../doc/image/net/rpc01.png)

<img src="./../doc/image/general-game-architect.jpg" width="70%" height="70%" alt="Server Architecture Diagram"/><br/>

### Ⅲ. Why fast
----------

- Use current best performin [zfoo protocol](protocol/README.md)  as serialization and deserialization protocol for
  gateway and RPC messages
- Lock-free design and elegant thread pool design, the user's request can always be guaranteed to be executed in the
  same thread of the same server through the gateway, so there is no need to use locks to ensure concurrency
- The rpc call is naturally asynchronous, and it is guaranteed to be executed in the same thread after the rpc
  asynchronous call ends, similar to the design idea of actor, especially suitable for scenarios with extremely high
  performance requirements
- Use the MVC design pattern to standardize development, ensure code quality, and execute efficiently

### Ⅳ. Server Architecture Evolution

- Single server +
  database，[zfoo net Single server usage tutorial](src/test/java/com/zfoo/net/core/tcp/server/TcpServerTest.java)

![Image text](../doc/image/net/framework01.png)

----------

- Add a reverse proxy, similar to a gateway

```
The first step in preparing for a larger scale architecture is to add a "reverse proxy"
Routing forwards the request to the correct endpoint
health examination
Authentication ensures that the user is actually allowed to access the server
Firewalls ensure users can only access the parts of the network we allow them to use
```

![Image text](../doc/image/net/framework02.png)

----------

- Introducing a load
  balancer， [zfoo net Gateway Load Balancing Tutorial](src/test/java/com/zfoo/net/core/gateway/GatewayTest.java)

```
The load balancer distributes the requests to the two servers. User 1 goes left, User 2 goes right, User 3 goes left again.
```

![Image text](../doc/image/net/framework03.png)

----------

- extended database

```
The integrated orm is based on mongodb, which can support distributed deployment
mongodb，Replica set deployment (master-slave mode), data synchronization, read-write separation,
mongodb，Fragmentation deployment, failover (disaster recovery)
```

![Image text](../doc/image/net/framework04.png)

----------

- microservice，[Microservice RPC Tutorial](src/test/java/com/zfoo/net/core/provider/ProviderTest.java)

```
As the development team grows with the application, more and more developers are working on the same server, and the potential for conflicts is high.
At this time, it is a better choice to split the business through microservices.

Microservices can be scaled individually to better adapt to demand. Development teams are independent from each other, each responsible for their own microservice lifecycle
Each microservice has its own resources, such as a database
```

![Image text](../doc/image/net/framework05.png)

----------

- Caching and Content Delivery Network (CDN)

```
A large part of web applications consists of static resources, such as images, CSS style files, Java scripts, and some pre-rendered pages for specific products, etc.
Game applications also contain a large number of image resources, 3D modeling resources
An enhanced version of the cache is called the Content Delivery Network (Content Delivery Network), a large number of caches all over the world.
This allows users to fetch web content from places that are physically close to them, rather than moving data from the source to the user each time.
```

![Image text](../doc/image/net/framework06.png)

----------

- message queue

```
When the traffic peaks, sometimes it is necessary to process a large number of flash sales such as Double 11, so that a large number of sudden requests can be queued up to process tasks
application decoupling
message distribution
```

![Image text](../doc/image/net/framework07.png)

----------

- Fragmentation, partitioning, such as assigning users to the closest server to provide services


![Image text](../doc/image/net/framework08.png)

----------

![Image text](../doc/image/net/framework09.png)

### Ⅴ. Dependent middleware zookeeper tutorial

- [Addition, deletion, modification and inspection of ZK basics](src/test/java/com/zfoo/net/zookeeper/base)
- [Curator additions, deletions, modifications](src/test/java/com/zfoo/net/zookeeper/curator)
- [Distributed auto-incrementing ID implemented by zookeeper](src/test/java/com/zfoo/net/zookeeper/recipes/atomicint)
- [Distributed Barrier](src/test/java/com/zfoo/net/zookeeper/recipes/distributedbarrier)
- [Distributed locks](src/test/java/com/zfoo/net/zookeeper/recipes/distributedbarrier)
- [Data publishing and subscribing](src/test/java/com/zfoo/net/zookeeper/recipes/nodecache)
- [Zookeeper's distributed election algorithm](src/test/java/com/zfoo/net/zookeeper/recipes/mastersel)

### Ⅵ. Reference article

- [Scaling webapps for newbs](https://arcentry.com/blog/scaling-webapps-for-newbs-and-non-techies/)
