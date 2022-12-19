# 服务包说明
### 这是家庭网盘服务端模块
```
- family-disk-service
    - family-disk-service-gateway 应用网关
    - family-disk-service-user 用户管理
    - family-disk-service-admin 后台管理
    - family-disk-service-file 文件管理
    - family-disk-service-notebook 记事本
    - family-disk-service-netdisk 网盘
    - family-disk-service-email 邮件发送服务
    - family-disk-service-model 数据库模型
    - family-disk-service-api rpc接口标准
    - family-disk-service-plugins 插件模块
    - family-disk-service-diary 日记
```

# 启动参数
### vm必须添加
```
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.math=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
--add-opens java.base/sun.net.util=ALL-UNNAMED
```
### 直连方式dubbo配置vm添加
```
-Ddubbo.resolve.file=D:\github\family-disk\family-disk-service\dubbo-resolve.properties
```
