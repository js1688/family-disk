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
    - family-disk-service-plugins 插件模块,不属于服务内,暂时没用
    - family-disk-service-bak 数据备份服务,暂时没有
```
## 关键配置文件说明
### application.yml
> 应用程序配置文件,除gateway模块外都需要配置数据库连接
> 
> user模块中的 user.space.init-size 配置项代表用户注册后默认创建的空间大小,单位MB
> 
> gateway模块中可以设置上传文件的大小,token签发有效时长,token生成的秘钥,跨域时允许访问的域名地址,白名单等
> 
> file模块 file.storage.path.temp 配置分片文件临时存放目录
> 
> file模块 file.storage.space 配置磁盘保留空间,单位GB,意思是磁盘不会被使用完,防止系统出问题
> 
> email模块 spring.mail 配置邮件发送服务地址,配置后程序发送的邮件都将通过这个账号发送出去
### dubbo-resolve.properties
> dubbo直连方式配置文件,程序启动的时候需要再vm中配置
> 
> 目前考虑规模的问题并没有使用注册中心,因为是使用springboot集成,想迁移到注册中心,也只需要通过配置就可以达到目的

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
## 修改配置文件,说明在上面👆👆👆👆👆
> 修改数据库连接配置
> 
> 修改分片文件存储的临时目录
> 
> 修改邮件发送服务地址

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
> 先通过页面注册一个普通账号,如没有部署页面,可以通过接口网关页面直接运行接口来注册
> 
> 注册成功后到后台数据库,找到表:user_info,字段:role,将它改成 ADMIN ,代表的是管理员账号
## 设置本地磁盘存储目录
> 在接口网关页面,右边有个 Authorize的按钮,带一把锁,这个是可以设置请求头部的,先调用用户管理中的登陆接口,登陆后返回token,将token设置到请求头部的Authorization中,这样就完成的登陆操作
> 
> 添加文件存储的物理磁盘路径,必须是登录后,在网关页面添加,在磁盘管理中,添加一个存储磁盘位置,type只能填LOCAL(只实现了这个),path输入磁盘目录
> 
> 为什么不把物理磁盘直接通过配置文件设置呢,这里主要是想后面扩展多种文件存储方式,不仅限于物理磁盘,而且一台机器也可以挂载多个盘,这个地方可以添加多个盘,它会将文件均匀的存储在多个盘中