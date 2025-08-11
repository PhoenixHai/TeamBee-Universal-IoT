#!/bin/bash

# 设置默认JVM参数
JAVA_OPTS="${JAVA_OPTS:-}"

# 内存配置
JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx2g"

# GC配置
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 时区设置
JAVA_OPTS="$JAVA_OPTS -Duser.timezone=Asia/Shanghai"

# 字符编码
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"

# 日志配置
JAVA_OPTS="$JAVA_OPTS -Dlogging.file.path=/app/logs"

# 配置文件路径
CONFIG_PATH="${CONFIG_PATH:-/app/config}"

# 启动应用
echo "Starting application with options: $JAVA_OPTS"
echo "Config path: $CONFIG_PATH"

exec java $JAVA_OPTS \
    -jar app.jar \
    --spring.config.location=file:$CONFIG_PATH/ \
    --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}