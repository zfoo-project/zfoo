# 一、卸载java

## 1. 卸载JDK安装路径

- java -version
- javac

```
判断jdk是否安装(显示版本号等信息，说明已经安装)
```

- which java（查看JDK的安装路径）

```
rm -rf JDK的安装路径
```

## 2. 删除环境变量

- java -version
- javac

```
判断jdk是否安装(显示版本号等信息，说明已经安装)
```

- vim /etc/profile，删除配置中的环境变量
- ls /etc，看看有没有别的java配置文件，如果有就全部删除

# 二、安装JDK

```
把JDK下载到/usr/local目录下，在/usr/local下新建文件夹java
```

- tar -zxvf openjdk-11+28_linux-x64_bin.tar.gz -C /usr/local
- rename jdk-11 java jdk-11

- vim /etc/profile

```
JAVA_HOME=/usr/local/java
JRE_HOME=$JAVA_HOME/lib
PATH=$JAVA_HOME/bin:$PATH
export JAVA_HOME JRE_HOME PATH
```

- source /etc/profile，加载环境变量

```
- java -version
- javac
- jps
判断jdk是否安装(显示版本号等信息，说明已经安装)
```
