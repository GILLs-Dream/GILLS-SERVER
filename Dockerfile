FROM openjdk:21-jdk
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app.jar"]