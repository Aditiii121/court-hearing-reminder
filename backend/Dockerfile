# =====================================================================
# Stage 1: Build
# =====================================================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml first for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source
COPY src ./src

# Build, skip tests (tests run in CI pipeline separately)
RUN mvn clean package -DskipTests -B

# =====================================================================
# Stage 2: Runtime
# =====================================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S courtapp && adduser -S courtapp -G courtapp

# Copy artifact from builder
COPY --from=builder /app/target/court-hearing-backend-*.jar app.jar

# Set ownership
RUN chown courtapp:courtapp app.jar

USER courtapp

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q -O /dev/null http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
