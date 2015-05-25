#! /bin/bash

export LANG="zh_CN.UTF-8"

cd ../

ADMIN_HOME=$(pwd)

CLASS_PATH="$ADMIN_HOME/lib/*:$ADMIN_HOME/dependencies/*:$ADMIN_HOME/conf"
LOG_PATH="$ADMIN_HOME/log"
LOG_FILE="$LOG_PATH/updateJadeConfig.log"

if ! test -d $LOG_PATH
	then mkdir $LOG_PATH
fi

java -cp $CLASS_PATH -DLOG_FILE=$LOG_FILE UpdateJadeConfig "$@"
