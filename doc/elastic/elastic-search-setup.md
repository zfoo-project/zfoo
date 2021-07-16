# 一、下载和安装

- 安装Java环境
- 安装ElasticSearch，在安装Java JDK，配置JAVA_HOME环境变量之后，就可以安装Elasticsearch全文搜索引擎了，首先需要下载特定版本的ES安装文件。
- 下载ElasticSearch最新版本，这里是7.x.x，从官方下载中心 ElasticSearch Download 下载ElasticSearch安装包，并解压

# 二、启动

## 1.Windows下ES安装

- 进入解压的bin/目录，双击执行 elasticsearch.bat，该脚本文件执行 ElasticSearch 安装程序
- 稍等片刻，打开浏览器，输入 http://localhost:9200，有回应说明安装成功

## 2.Linux下ES安装

- 将elasticsearch-7.7.0-linux-x86_64.tar.gz下载到/usr/local/目录，这个目录是专门放下载的第三方软件的目录
- tar -xzvf elasticsearch-7.7.0-linux-x86_64.tar.gz 解压
- elasticsearch.yml做如下配置：

```
node.name: node-1
network.host: 0.0.0.0
http.port: 9200
cluster.initial_master_nodes: ["node-1"]

path.data: /data/es/db
path.logs: /data/es/logs
```

- mkdir -p /data/es/db /data/es/logs

- jvm.options做如下配置，单机部署可不用设置：

```
-Xms4g
-Xmx4g
```

- Elasticsearch 不允许root用户直接运行，所以要创建新用户，在root用户中创建新用户,执行如下命令：

```
useradd sun  # 新增sun用户
passwd  sun  # 为sun用户设置密码
chown -R sun:sun /usr/local/elasticsearch-7.7.0 # 为新用户授权，文件夹所有者
chown -R sun:sun /data/es
su sun
./elasticsearch &
```

- 可能遇到的问题

```
启动内存国小，设置jvm内存-xms设置为内存的一半

max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
vim /etc/sysctl.conf        # 在最后一行上加上
vm.max_map_count=262144     # 保存退出，加载配置，启动容器，就能够访问了
sysctl -p                   # 使修改生效
```

- 验证是否生效，curl localhost:9200

# 三、插件和工具

## 1.elasticsearch-analysis-ik插件

- ik是一个对中文比较友好的分词插件

```
download pre-build package from here: https://github.com/medcl/elasticsearch-analysis-ik/releases
create plugin folder cd your-es-root/plugins/ && mkdir ik
unzip plugin to folder your-es-root/plugins/ik
```

## 2.kibanba安装

- kibanba是es中的数据可视化工具

```
https://www.elastic.co/cn/downloads/kibanasu sun

直接下载解压，运行bin目录下的可执行文件kibana.bat
```
