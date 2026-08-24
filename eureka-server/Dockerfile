#build
FROM maven:4.0.0-rc-5-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true

#deploy
FROM eclipse-temurin:17-jre-alpine-3.24

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8761

CMD ["java", "-jar", "app.jar"]