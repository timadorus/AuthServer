#!/bin/sh
SERVICE_NAME=TimadorusAuthServer
WORKING_DIR=/usr/share/timadorus-auth-server
PATH_TO_JAR=/usr/share/timadorus-auth-server/auth-server.jar
JAR_PARAMS=/usr/share/timadorus-auth-server/
PID_PATH_NAME=/tmp/TimadorusAuthServer-pid
LOG_FILE=authserver.log
case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            cd $WORKING_DIR
            nohup java -jar $PATH_TO_JAR $JAR_PARAMS /tmp 2>> $LOG_FILE >> $LOG_FILE &
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ..."
            kill $PID;
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            cd $WORKING_DIR
            nohup java -jar $PATH_TO_JAR $JAR_PARAMS /tmp 2>> $LOG_FILE >> $LOG_FILE &
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    force-restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
        fi
        echo "$SERVICE_NAME starting ..."
        cd $WORKING_DIR
        nohup java -jar $PATH_TO_JAR $JAR_PARAMS /tmp 2>> $LOG_FILE >> $LOG_FILE &
        echo $! > $PID_PATH_NAME
        echo "$SERVICE_NAME started ..."
    ;;
esac
