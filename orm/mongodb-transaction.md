# 一、安全写入数据

## 1.MongoDB的四种写入级别

- Unacknowledged 非确认式写入

```
{ writeConcern:{w:0}}
非确认式写入不返回响应结果。
```

- Acknowledged 确认式写入，mogodb客户端默认的写入模式

```
确认式写入返回写入失败的错误信息，比如DuplicateKey Error。使用命令:
{ writeConcern:{w:1}}
```

- Journaled 日志写入

```
一般的写入完成只是写入到内存中，并没有持久化到硬盘，日志写入模式会写入完成之后把记录保存到journal日志后才返回响应结果，这种写入方式能够承受服务器突然断电崩溃，更有效的保障数据的安全。
{ writeConcern:{j:true}}
```

- Replica Acknowledged 复制集确认式写入

```
写操作不仅要得到主节点的写入确认，还需要得到从节点的写入确认，这里还可以设置写入节点的个数。这种方式适用于对写入安全要求更高的场景。
{ writeConcern:{w:2}}
```

# 二、Journal日志

## 1.单机开启Journal，多机器使用副本集

```
Journal是MongoDB中的redolog（MySQL），而Oplog则负责副本集的同步（binlog MySQL）
不开启Journal，数据会写入内存，一般每60秒写入一次到硬盘
开启Journal，每100毫秒左右往Journal日志文件写入一次数据（顺序写速度快），即使宕机，结果Journal恢复也只丢死100毫秒数据
```

## 2.生产环境不要信任repair恢复的数据

```
如果数据库崩溃了，而且没有开启--journal的情况下，千万不要这些数据拿来就用，因为数据库能损坏，索引可能混乱。

如果开启了Journal就可以使用mongod --repair修复一次。修复过程是将所有的文档导出后马上导入，忽略无效的文档，完成后会重建索引。
但是这不是最优的选择，因为可能会丢失数据而且需要大量的磁盘空间。repair是最差的做法，比较好的做法是用以前备份过的数据库恢复，或者使用副本集部署。
```

# 三、锁

- 查看锁的情况

```
db.serverStatus()
db.currentOp()
```

- 解锁

```
db.currentOp()      # 找出有锁状态的执行操作，找到opid，根据之前获取的opid
db.killOp(opid)     # kill掉对应的操作
```
