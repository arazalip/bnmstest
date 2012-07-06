VER=1.0
MAIN_CLASS=com.bourse.nms.Launcher

MEM_MIN=1024m
MEM_MAX=6144m

JAVA_OPTS="-server -Xms$MEM_MIN -Xmx$MEM_MAX -XX:+UseParallelGC -XX:+AggressiveOpts -XX:+UseFastAccessorMethods"
JAVA_OPTS="$JAVA_OPTS -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -XX:MaxPermSize=128M"

#remove comment to enable remote debugging
#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8785,server=y,suspend=n"

CP=target/:target/bnmstest-$VER/WEB-INF/classes
LIB=target/bnmstest-$VER/WEB-INF/lib
for a in $LIB/*.jar; do
    CP=$CP:$a
done

export CLASSPATH=$CP
echo "running with: $JAVA_OPTS $OPTS $MAIN_CLASS"
java $JAVA_OPTS $OPTS $MAIN_CLASS
