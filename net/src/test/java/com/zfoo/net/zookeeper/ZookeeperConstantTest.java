package com.zfoo.net.zookeeper;

/**
 * 引子和趣闻：
 * Zookeeper名字的由来是比较有趣的，下面的片段摘抄自《从PAXOS到ZOOKEEPER分布式一致性原理与实践》一书：
 * Zookeeper最早起源于雅虎的研究院的一个研究小组。在当时，研究人员发现，在雅虎内部很多大型的系统需要依赖一个类似的系统进行分布式协调，
 * 但是这些系统往往存在分布式单点问题。所以雅虎的开发人员就试图开发一个通用的无单点问题的分布式协调框架。
 * 在立项初期，考虑到很多项目都是用动物的名字来命名的(例如著名的Pig项目)，雅虎的工程师希望给这个项目也取一个动物的名字。
 * 时任研究院的首席科学家Raghu Ramakrishnan开玩笑说：再这样下去，我们这儿就变成动物园了。
 * 此话一出，大家纷纷表示就叫动物园管理员吧——因为各个以动物命名的分布式组件放在一起，雅虎的整个分布式系统看上去就像一个大型的动物园了，
 * 而Zookeeper正好用来进行分布式环境的协调——于是，Zookeeper的名字由此诞生了。
 * <p>
 * Curator无疑是Zookeeper客户端中的瑞士军刀，它译作"馆长"或者''管理者''，不知道是不是开发小组有意而为之，
 * 笔者猜测有可能这样命名的原因是说明Curator就是Zookeeper的馆长(脑洞有点大：Curator就是动物园的园长)。
 * <p>
 * curator-framework：对zookeeper的底层api的一些封装
 * curator-client：提供一些客户端的操作，例如重试策略等
 * curator-recipes：封装了一些高级特性，如：Cache事件监听、选举、分布式锁、分布式计数器、分布式Barrier等
 *
 * @author jaysunxiao
 * @version 1.0
 * @since 2018-04-03 15:29
 */

public abstract class ZookeeperConstantTest {

    public static String URL = "localhost:2181";

}
