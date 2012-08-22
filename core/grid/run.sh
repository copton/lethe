#!/bin/sh
PYTHON=/usr/bin/python

JAVA=/usr/bin/java

JAVA_CLASSPATH=.
for jar in ../../../libs/*.jar; do 
    JAVA_CLASSPATH=$JAVA_CLASSPATH:$jar
done
JAVA_CLASSPATH=$JAVA_CLASSPATH:/usr/share/java/db.jar

command=${0##*/run-}

case $command in
    breeze)
        cd ../stormlog
        $PYTHON main.py "$@"
        ;; 
   manager)
        cd ../manager 
        java -Djava.library.path=/usr/lib -classpath $JAVA_CLASSPATH:manager.jar:comm.jar server.ManagerServer "$@"
        ;;
    authentication)
        cd ../authservice 
        java -classpath $JAVA_CLASSPATH:authentication.jar:comm.jar authentication/AuthenticationServer "$@"
        ;; 
   generator)
        cd ../generator
        $PYTHON main.py "$@"
        ;;
	
   codedistribution)
	cd ../sourceservice
	java -Djava.library.path=/usr/lib -classpath $JAVA_CLASSPATH:comm.jar:icepatch.jar server/SourceServer "$@" "-default"
	;;
    *)
        echo "don't know how to run $command"
        exit 1
        ;;
esac
