FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Use JDK 21 for the final runtime stage
FROM eclipse-temurin:21-jdk-jammy
COPY --from=build /target/*.jar demo.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","demo.jar"]