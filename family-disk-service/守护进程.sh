#todo 还没写好,后面程序搞好了再弄这个守护进程脚本
#!/bin/bash
while true; do
    #9200是es监听的端口；
    port=`netstat -an | grep 1400 | wc -l`
    #上述代码表示如果9200起来了，就会至少显示1行，$port>1；如果es没有起来，就是9200没有被监听，就不会有显示，所以$port<1
    #判断：如果$port小于1，说明es服务没有起来，重启es服务
    if [ $port -lt 1 ]; then
        echo '重启服务'
        sh /root/love/love-api/start_love_api.sh
    fi
        #每次循环沉睡10s
        echo '服务正常'
        sleep 30
done