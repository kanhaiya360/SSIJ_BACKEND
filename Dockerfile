# ----------- Stage 1: Build Stage -----------
FROM maven:3.9.4-eclipse-temurin-21 AS build

# Set working directory inside the container
WORKDIR /app

# Copy pom.xml first (to cache dependencies)
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the project and skip tests for faster build
RUN mvn clean package -DskipTests

# ----------- Stage 2: Runtime Stage -----------
FROM eclipse-temurin:21-jdk

# Set working directory for runtime
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar ./app.jar

# Expose port (optional, if your app is a web service)
EXPOSE 8080

# Run the Java application
ENTRYPOINT ["java", "-jar", "app.jar"]