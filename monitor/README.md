English | [简体中文](./README_CN.md)

### Ⅰ. Introduction

- [monitor](https://github.com/zfoo-project/zfoo/blob/main/storage/README.md) Lightweight cpu, memory, hard disk,
  network monitoring

### Ⅱ. Use

- uptime, imitating the uptime command of Linux, can be used to monitor the load of cpu
  ```
  UptimeVO uptime = OSUtils.uptime();
  ```

- df, imitating the Linux df command, can be used to monitor the hard disk capacity
  ```
  List<DiskFileSystemVO> df = OSUtils.df();
  ```


- free, imitating Linux's free command, can be used to monitor memory usage
  ```
  MemoryVO free = OSUtils.free();
  ```


- sar, the sar command imitating Linux, can be used to monitor network IO
  ```
  List<SarVO> sar = OSUtils.sar();
  ```

### Ⅲ. Traditional Server Monitoring

- Traditional stand-alone server monitoring mainly depends on cpu, memory, hard disk, network
  ![Image text](../doc/image/monitor/monitor01.png)

- Distributed server monitoring is mainly to monitor multiple servers, which is not much different from stand-alone
  monitoring.
  ```
  It is very simple to write a distributed server monitoring by yourself. 
  You only need to collect the main data of the current server every second or minute, and upload it to the database or other servers.
  Use these collected data to analyze performance, make monitoring alarms, make line charts or other visual charts.
  Monitor can be easily collected through static class encapsulation, and then realize its own distributed monitoring program, 
  and can be completely embedded in Java programs without additional deployment
  ```

### Ⅲ. Modern Server Monitoring

- Now that the server is the era of containers, server monitoring has also changed from server monitoring to container
  monitoring
- Container monitoring is almost an integrated function of the container itself, and even cloud vendors will provide
  monitoring programs and monitoring services for free

### Ⅳ. Server Monitoring We Can Do

- Self-customized monitoring is the monitoring that fits your own project, such as implementing a monitoring that
  monitors the number of times the rpc interface is called by yourself
