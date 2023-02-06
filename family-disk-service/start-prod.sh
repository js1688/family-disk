#!/bin/bash
# 启动脚本
JAVA_OPT="${JAVA_OPT} -server -Xms512m -Xmx512m"
JAVA_OPT="${JAVA_OPT} -ea"
JAVA_OPT="${JAVA_OPT} --add-opens java.base/java.lang=ALL-UNNAMED"
JAVA_OPT="${JAVA_OPT} --add-opens java.base/java.math=ALL-UNNAMED"
JAVA_OPT="${JAVA_OPT} --add-opens java.base/java.util=ALL-UNNAMED"
JAVA_OPT="${JAVA_OPT} --add-opens java.base/sun.net.util=ALL-UNNAMED"
JAVA_OPT="${JAVA_OPT} -Ddubbo.resolve.file=dubbo-resolve.properties"
JAVA_OPT="${JAVA_OPT} -Ddubbo.consumer.check=false"

cmd=$1
profiles=$2

if [ ! $cmd ]; then
  echo "Please specify args 'start|restart|stop'"
  exit
fi

array=(
family-disk-service-email/target/family-disk-service-email.jar
family-disk-service-user/target/family-disk-service-user.jar
family-disk-service-admin/target/family-disk-service-admin.jar
family-disk-service-file/target/family-disk-service-file.jar
family-disk-service-netdisk/target/family-disk-service-netdisk.jar
family-disk-service-journal/target/family-disk-service-journal.jar
family-disk-service-notebook/target/family-disk-service-notebook.jar
family-disk-service-gateway/target/family-disk-service-gateway.jar
)
# shellcheck disable=SC2068
for app in ${array[@]};
do
    pid=`ps -ef|grep java|grep $app|awk '{print $2}'`

    startup(){
      echo "java ${JAVA_OPT}  -jar $app --spring.profiles.active=$profiles >/dev/null 2>&1 &"
      nohup java ${JAVA_OPT}  -jar $app --spring.profiles.active=$profiles >/dev/null 2>&1 &
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