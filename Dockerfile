FROM maven:3.8.4-jdk-8 AS builder
WORKDIR /app
COPY settings.xml /usr/share/maven/conf/settings.xml
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:8-jdk
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
