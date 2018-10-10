#!/usr/bin/env bash

./gradlew build

docker build -t talbotindustries/iot-spear-agent-java-base --file Dockerfile.base .

docker build -t talbotindustries/iot-spear-agent-java .

# To run within Docker:
# docker run -d --name iot-spear-agent-java -e IOT_SPEAR_DEVICE_ACCESS_ID=xxxxx IOT_SPEAR_DEVICE_HOST_NAME=http://somehost iot-spear-agent-java
