#!/bin/bash
# 阿里云云效自动化部署启动脚本
JAVA_OPT="${JAVA_OPT} -server -Xms512m -Xmx512m"
JAVA_OPT="${JAVA_OPT} -ea"
JAVA_OPT="${JAVA_OPT} --add-opens java.base/java.lang=ALL-UNNAMED"
JAVA_OPT="${JAVA_OPT} --add-opens java.base/java.math=ALL-UNNAMED"
JAVA_OPT="${JAVA_OPT} --add-opens java.base/java.util=ALL-UNNAMED"
JAVA_OPT="${JAVA_OPT} --add-opens java.base/sun.net.util=ALL-UNNAMED"
JAVA_OPT="${JAVA_OPT} -Ddubbo.resolve.file=dubbo-resolve.properties"

cmd=$1

if [ ! $cmd ]; then
  echo "Please specify args 'start|restart|stop'"
  exit
fi

array=(
family-disk-service-email-0.0.1-SNAPSHOT.jar
family-disk-service-file-0.0.1-SNAPSHOT.jar
family-disk-service-netdisk-0.0.1-SNAPSHOT.jar
family-disk-service-admin-0.0.1-SNAPSHOT.jar
family-disk-service-user-0.0.1-SNAPSHOT.jar
family-disk-service-gateway-0.0.1-SNAPSHOT.jar
)
# shellcheck disable=SC2068
for app in ${array[@]};
do
    pid=`ps -ef|grep java|grep $app|awk '{print $2}'`

    startup(){
      nohup java ${JAVA_OPT}  -jar $app --spring.profiles.active=prod >/dev/null 2>&1 &
    }

    if [ $cmd == 'start' ]; then
      if [ ! $pid ]; then
        startup
      else
        echo "$app is running! pid=$pid"
      fi
    fi

    if [ $cmd == 'restart' ]; then
      if [ $pid ]
        then
          echo "$pid will be killed after 3 seconds!"
          sleep 3
          kill -9 $pid
      fi
      startup
    fi

    if [ $cmd == 'stop' ]; then
      if [ $pid ]; then
        echo "$pid will be killed after 3 seconds!"
        sleep 3
        kill -9 $pid
      fi
      echo "$app is stopped"
    fi
done