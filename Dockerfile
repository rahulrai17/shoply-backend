# Stage 1: Build the application using Maven Wrapper inside Docker
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Maven Wrapper files and pom.xml first to leverage Docker layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source code and build executable JAR (skipping tests during Docker build layer)
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Production-ready lightweight runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create unprivileged system user for container security compliance
RUN addgroup -S shoply && adduser -S shoply -G shoply
USER shoply:shoply

# Copy built JAR artifact from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose default Spring Boot application port
EXPOSE 8080

# Environment variables with sensible production defaults
ENV DB_URL=jdbc:postgresql://postgres:5432/shoplydb
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=postgres
ENV JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
ENV JWT_EXPIRATION=86400000

# Container entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
