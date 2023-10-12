# 前言
> 我经常需要使用到云存储,云备忘录,偶尔还要写日记,目前市面上的商用产品并没有一次性集成我所需要的功能,我也并不想使用商用服务,因为它总是会存在一些问题让我不想使用它,不放心使用它,我自己本身也是一个程序员,所以想开发一套满足我日常使用的服务,将它命名为(家庭网盘)

# 对比商用
|对比项|家庭网盘|商用网盘|其它开源网盘|
|-|-|-|-|
|广告|无|大部分有|无|
|限速|无|大部分有|无|
|功能|网盘,日记,备忘录|大部分功能单一|功能单一|
|在线预览文件|视频,图片,音频,兼容苹果设备拍摄的内容|部分不支持|部分不支持|
|扩容成本|低|高|低|
|安全分享|有|有|有|
|局域网加速|有|无|没看到有|
|设备类型限制|有浏览器即可|部分有|部分有|
|隐私|私有磁盘|公共磁盘会扫描是否有违法数据|私有磁盘|
|服务器运维成本|高|无|高|
|技术要求|有|无|有|
|技术高度|低|高|高|
|开源|是|否|是|
|浏览器上传下载限制|支持超大文件上传下载|部分有|部分有|
|兼容移动设备|是|是|部分是|
|离线下载|有(借助[aria2](https://github.com/P3TERX/aria2.sh))|部分有|部分有|
|WebDAV协议|有|有|有|

## 家庭网盘使用介绍
> 以家庭为服务单位,实际上也支持saas化,做好了注册和数据安全隔离,提供的功能有(日记,网盘,备忘录)
> 
> 网页版介绍视频[点击跳转到系统演示视频](https://www.bilibili.com/video/BV1fP411X72v/)
> 
> webdav挂载介绍[视频还未录制好]()
## 后期规划
> 功能大概是不会再增加了,后面的更新应该是围绕着这些功能做优化,改bug,支持多种设备使用,主要是app在同步数据上很方便,目前只支持浏览器访问,因为已经实现了超大文件的上传下载,所以不会急着开发app
> 
> 如有新功能会在对比中增加

## 架构介绍
本系统采用前后端分离方式：
* 前端：vue （v3.2.45）+ vant-ui（v4.0.3）+ naive-ui（2.34.3） + vite（v4.0.0）[点击跳转前端架构详细介绍](https://github.com/js1688/family-disk/tree/main/family-disk-html#readme) 
* 后端：微服务设计 + dubbo(v3.1.3) + java（v17）+ springboot（v2.7.6）+ maven（v3.6.1）[点击跳转后端架构详细介绍](https://github.com/js1688/family-disk/tree/main/family-disk-service#readme) 
* 数据库：mysql（v8.0.31）[点击跳转表模型DDL文件](https://github.com/js1688/family-disk/tree/main/mysql-table-model/DDL)
## 系统部署方案场景推荐
> 注意：这并不是直接告诉你如何部署，这只是以作者的经验向你介绍哪种场景适合怎样的部署方案，想知道部署时需要修改哪些配置，还是需要先仔细阅读前后端架构详细介绍，再来执行部署。

|部署方式|优势|劣势|
|-|-|-|
|局域网|安全性最高，同时访问速度最快|不可以公网访问|
|云服务器部署|公网可以访问,无服务器电费,24*7稳定服务,不需要运维服务器机器|隐私低,会被扫描存储文件,扩容费用高,增加宽带费用高,续期费用高,数据迁移速度慢|
|公网穿透到局域网|隐私高,扩容费用低,数据不需要迁移,仅仅换穿透服务即可,穿透服务推荐使用[点击跳转到frp官网](https://gofrp.org/docs/overview/)|有服务器电费,不能24*7稳定服务,需要运维服务器机器,依然需要购买公网机器做跳板机,但是可以买低配置费用相对低|
|家庭宽带ipv4|无穿透,隐私高,扩容费用低,访问速度快,数据无需迁移|有服务器电费,不能24*7稳定服务,需要服务器运维,主要是从运营商获取ipv4基本是不可行的|
|家庭宽带ipv6(推荐)|无穿透,隐私高,扩容费用低,访问速度快,数据无需迁移|有服务器电费,不能24*7稳定服务,需要服务器运维|
