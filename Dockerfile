FROM gradle:8.5-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:21-slim
COPY --from=build /home/gradle/src/build/libs/*.jar /app/sql-parser.jar

CMD ["java", "-jar", "/app/sql-parser.jar"]