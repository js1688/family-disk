# 前言
> 家庭网盘后端模块,由java语言开发,可以运行在Windows,macOS,linux内核的操作系统上
> 
> 因为是使用springboot做的集成,在原生配置允许的范围内,修改任何配置都会生效,比如直接通过配置将dubbo直连变成接入到注册中心
> 
> 使用jwt认证方式,开放api网关,面向客户端可以是app,网页,pc端程序,都可以支持
## 模块目录
```
- family-disk-service
    - family-disk-service-gateway 应用网关,http,websocket,
    - family-disk-service-manage 页面管理服务,页面增删改查,
    - family-disk-service-file 文件服务,负责文件二进制流读写,
    - family-disk-service-model 数据库模型,mysql po类,mapper类
    - family-disk-service-api rpc接口标准,dubbo接口定义,以及dto类定义
    - family-disk-service-scheduling 定时任务,做一些清理,备份相关的工作,它不是dubbo服务,也不会调用dubbo接口
    
    - family-disk-service-plugins 插件模块,不属于服务内,暂时没用
```
## 关键配置文件说明
### application.yml
> 配置详情见配置内,行内注释
### dubbo-resolve.properties
> dubbo直连方式配置文件,程序启动的时候需要再vm中配置
### logback.xml
> 日志配置文件
### start-app.sh
> 程序启动脚本,只是提供,可以不使用它

# 启动参数
## vm必须添加
```
//下列参数解决jdk版本过高引起的问题
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.math=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
--add-opens java.base/sun.net.util=ALL-UNNAMED

//配置dubbo直连配置文件
-Ddubbo.resolve.file=D:\github\family-disk\family-disk-service\dubbo-resolve.properties
```

# 启动程序
## 环境准备
```
jdk 17
maven 3.6.1
dubbo v3.1.3
springboot v2.7.6
mysql 8.0.31
```
## 需要修改配置文件
### 配置数据库连接
> 在 manage,stream,scheduling,模块中都需要修改
### 配置分片文件临时存储目录
> 在 stream,scheduling,模块中修改
### 配置邮箱发送服务器
> 在 manage,模块中修改

## 编译
```shell
cd family-disk-service;
mvn -B clean package -Dmaven.test.skip=true -Dautoconfig.skip;
```
## 启动
```shell
sh start-app.sh start;
```
## 日志
> 启动后会在当前目录生成logs文件夹,下面是各模块的日志
## 接口网关页面
> http://127.0.0.1:8800/swagger-ui/index.html
## 注册管理员账号
> 可以通过部署前端页面注册,或者在swagger页面调用接口注册,第一个注册的用户,默认为管理员账号
## 使用swagger接口页面,设置本地磁盘存储目录
### 先使用管理员账号登录
> 在接口网关页面,右边有个 Authorize的按钮,带一把锁,这个是可以设置请求头部的,先调用用户管理中的登陆接口,登陆后返回token,将token设置到请求头部的Authorization中,这样就完成的登陆操作
### 添加一个物理磁盘路径
> 添加文件存储的物理磁盘路径,必须是登录后,在网关页面添加,在磁盘管理中,添加一个存储磁盘位置,type只能填LOCAL(只实现了这个),path输入磁盘目录