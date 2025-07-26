FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN mvn dependency:go-offline -B
COPY src src
RUN mvn package -Dmaven.test.skip=true

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/greenlink-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/uploads/products
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod", "--server.port=${PORT:-8080}"]