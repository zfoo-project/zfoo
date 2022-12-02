English | [简体中文](./README_CN.md)

### Ⅰ. Introduction

- [event](https://github.com/zfoo-project/zfoo/blob/main/event/README.md) The event bus is used to decouple different
  modules and improve the quality of the code. The core idea is the observer design pattern

### Ⅱ. Event bus at the hardware level

#### 1. Between devices communication


![Image text](../doc/image/event/device01.png)
![Image text](../doc/image/event/device02.png)

#### 2. Reduced complexity with the bus

![Image text](../doc/image/event/device03.png)
![Image text](../doc/image/event/device04.png)
![Image text](../doc/image/event/device05.png)

#### 3. Multiple Architectures of Hardware Buses

![Image text](../doc/image/event/device06.png)
![Image text](../doc/image/event/device07.png)

The core of the design idea of the bus is to reduce the complexity and coupling degree of interaction between multiple
modules.

At the actual hardware level, a bus is actually a set of wires connecting circuits.
Because of the differences in speed between different devices, there are often multiple buses in a computer.

In fact, the design idea of the bus is often used in our software development process.
The event bus is a common design pattern. Usually, the event bus will also be combined with the subscriber-publisher
mode to
become a main mode of interaction between loosely coupled modules of a large-scale system.

### Ⅲ. Event bus at software level

![Image text](../doc/image/event/event01.png)
![Image text](../doc/image/event/event02.png)

- The event bus is called the observer mode from the design mode
- There is no fixed way to write the design pattern, zfoo event implements the event bus with the simplest annotation
- zfoo event uses Javassist bytecode enhancement technology to dynamically proxy event receivers, avoiding reflection
  and no performance loss