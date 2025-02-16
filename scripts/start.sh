#!/usr/bin/env bash

PROJECT_ROOT="/home/ubuntu/app"
JAR_FILE="$PROJECT_ROOT/spitching-be-0.0.1-SNAPSHOT.jar"
BUILD_JAR="$PROJECT_ROOT/build/libs/spitching-be-0.0.1-SNAPSHOT.jar"

APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

# 디렉토리 존재 확인
echo "$TIME_NOW > Checking directory structure:" >> $DEPLOY_LOG
ls -la $PROJECT_ROOT >> $DEPLOY_LOG
ls -la $PROJECT_ROOT/build/libs >> $DEPLOY_LOG

# build 파일 복사
echo "$TIME_NOW > Copying JAR file" >> $DEPLOY_LOG
if [ -f $BUILD_JAR ]; then
    cp $BUILD_JAR $JAR_FILE
    echo "$TIME_NOW > JAR file copied successfully" >> $DEPLOY_LOG
else
    echo "$TIME_NOW > ERROR: JAR file not found in build/libs" >> $DEPLOY_LOG
    exit 1
fi

# jar 파일 실행
echo "$TIME_NOW > Starting application" >> $DEPLOY_LOG
nohup java -jar $JAR_FILE > $APP_LOG 2> $ERROR_LOG &

CURRENT_PID=$(pgrep -f $JAR_FILE)
echo "$TIME_NOW > Application started with PID $CURRENT_PID" >> $DEPLOY_LOG