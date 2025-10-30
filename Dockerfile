FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /workspace
COPY . .
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: Run with JRE 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Ensure this matches your JAR file name in the target/ directory
COPY --from=builder /workspace/target/holiday-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]