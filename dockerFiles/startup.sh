#!/bin/sh

# Start the server
cd /app/libs

if [[ -z "${JVM_MEMORY_SETTINGS}" ]]; then
  JVM_MEMORY="-Xms512M -Xmx750M"
else
  JVM_MEMORY="${JVM_MEMORY_SETTINGS}"
fi

echo "Starting grpcbin with $JVM_MEMORY memory settings"
export LD_PRELOAD=/lib/libgcompat.so.0:/usr/lib/libunwind.so.8

nohup java $JVM_MEMORY -XX:+UseG1GC -jar -DCONDUCTOR_CONFIG_FILE=$config_file server.jar