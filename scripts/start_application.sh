#!/bin/bash
cd /home/ubuntu/app

# 기존 프로세스 종료
pid=$(pgrep -f spitching-be)
if [ -n "$pid" ]; then
    echo "Killing existing process: $pid"
    kill -9 $pid
fi

chmod +x ./gradlew
./gradlew build

# 새로운 프로세스 시작
nohup java -jar build/libs/*.jar > /dev/null 2> /dev/null < /dev/null &