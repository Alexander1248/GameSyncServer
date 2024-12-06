FROM openjdk:21-jdk
COPY build/libs/GameServer-1.0.0.jar GameServer-1.0.0.jar
ENTRYPOINT ["java","-jar","/GameServer-1.0.0.jar"]