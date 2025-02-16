#!/bin/bash
cd /home/ubuntu/app

chmod +x ./gradlew
./gradlew build

nohup java -jar build/libs/*.jar > /dev/null 2> /dev/null < /dev/null &