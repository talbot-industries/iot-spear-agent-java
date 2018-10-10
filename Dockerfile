FROM openjdk:8-jre-alpine

MAINTAINER Talbot Industries Ltd <info@talbot-industries.com>

COPY build/libs/iot-spear-agent-java*.jar /root/iot-spear-agent-java.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/root/iot-spear-agent-java.jar"]
