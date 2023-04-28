# 前言
> 前端分为:移动端(vant-ui),pc端(naive-ui),两种交互方式,应该配置nginx自动识别访问设备
> 
> 前端所有的上传和下载均为分片方式,所以它支持超大文件的上传与下载
>
> 每次进入页面程序会尝试连接内网服务地址,以达到最快连接速度,如内网地址不可访问,则是使用公网地址访问
> 
> 因为家庭网盘推荐是部署在自己家里的机器,移动设备大多数情况下是可以与网盘服务端处于同一网络,所以才做出这项功能
> 
> 如果公网服务部署了https,内网服务地址也需要部署https,因为浏览器限制了同源,[点击阅读内网部署https方法](https://blog.jflove.cn/2023/01/30/%E5%B1%80%E5%9F%9F%E7%BD%91%E5%AE%89%E5%85%A8%E7%9A%84https%E5%8D%8F%E8%AE%AE%E8%A7%A3%E5%86%B3%E6%96%B9%E6%A1%88.html)
# 重要配置
> 在 src/global/KeyGlobal.js 文件中有2个重要配置
> 
> data.baseURL 配置的是公网服务地址
> 
> data.lanURL 配置的是内网服务地址

# 环境准备
```
npm 8.19.2
vite 4.0.2 
darwin-x64 
node v18.12.1
@vue/cli 4.3.1
```
# 开始
## 编译
```sh
npm run build
```
## 启动
```sh
npm run dev
```
## 部署
> 编译后,dist文件夹就是应用静态文件,可以使用nginx提供服务端口,指向这个编译后的静态文件

## nginx配置例子
``` 
    #家庭网盘-PC端
    server {
        listen       80;
        server_name  jflove.cn;
        rewrite ^(.*)$  https://www.jflove.cn$1 permanent;
    }
    server {
        listen       443;
        server_name  jflove.cn;
        rewrite ^(.*)$  https://www.jflove.cn$1 permanent;
    }
    server {
        listen       80;
        server_name  www.jflove.cn;
        rewrite ^(.*)$  https://www.jflove.cn$1 permanent;
    }
    server {
        # 根目录
        root /root/html/family-disk-html/dist;

        # Load configuration files for the default server block.
        include /etc/nginx/default.d/*.conf;

        listen 443;
        server_name www.jflove.cn; #填写绑定证书的域名
        ssl on;
        ssl_certificate /root/ssl/www.jflove.cn_nginx/www.jflove.cn_bundle.crt;
        ssl_certificate_key /root/ssl/www.jflove.cn_nginx/www.jflove.cn.key;
        ssl_session_timeout 5m;
        ssl_protocols TLSv1 TLSv1.1 TLSv1.2; #按照这个协议配置
        ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;#按照这个套件配置
        ssl_prefer_server_ciphers on;
        # 下面根据user_agent可以获取
        #移动端跳转到m
        if ($http_user_agent ~* (mobile|nokia|iphone|ipad|android|samsung|htc|blackberry)) {
                rewrite  ^(.*)    https://m.jflove.cn$1 permanent;
        }
        # 匹配协议
        location / {
            # 需要指向下面的 @router 否则会出现 Vue 的路由在 Nginx 中刷新出现 404
            try_files $uri $uri/ @router;
            index /src/pc/index.html/index.html;
        }
        # 对应上面的 @router，主要原因是路由的路径资源并不是一个真实的路径，所以无法找到具体的文件
        # 因此需要 rewrite 到 index.html 中，然后交给路由在处理请求资源
        location @router {
            rewrite ^.*$ /src/pc/index.html last;
        }
    }

    #家庭网盘-移动端
    server {
        listen       80;
        server_name  m.jflove.cn;
        rewrite ^(.*)$  https://m.jflove.cn$1 permanent;
    }
    server {
        # 根目录
        root /root/html/family-disk-html/dist;

        # Load configuration files for the default server block.
        include /etc/nginx/default.d/*.conf;

        listen 443;
        server_name m.jflove.cn; #填写绑定证书的域名
        ssl on;
        ssl_certificate /root/ssl/m.jflove.cn_nginx/m.jflove.cn_bundle.crt;
        ssl_certificate_key /root/ssl/m.jflove.cn_nginx/m.jflove.cn.key;
        ssl_session_timeout 5m;
        ssl_protocols TLSv1 TLSv1.1 TLSv1.2; #按照这个协议配置
        ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;#按照这个套件配置
        ssl_prefer_server_ciphers on;
        # 下面根据user_agent可以获取
        #非移动端跳转到www
        if ($http_user_agent !~* (mobile|nokia|iphone|ipad|android|samsung|htc|blackberry)) {
                rewrite  ^(.*)    https://www.jflove.cn$1 permanent;
        }
        # 匹配协议
        location / {
            # 需要指向下面的 @router 否则会出现 Vue 的路由在 Nginx 中刷新出现 404
            try_files $uri $uri/ @router;
            index /src/mobile/index.html;
        }
        # 对应上面的 @router，主要原因是路由的路径资源并不是一个真实的路径，所以无法找到具体的文件
        # 因此需要 rewrite 到 index.html 中，然后交给路由在处理请求资源
        location @router {
            rewrite ^.*$ /src/mobile/index.html last;
        }
    }
```

