# ---------- Build stage ----------
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline

# Build
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Run stage ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create non-root user + uploads dir BEFORE switching users
RUN useradd --system --uid 1001 appuser \
    && mkdir -p /app/uploads \
    && chown -R appuser:appuser /app

COPY --from=build /app/target/walk-with-god-api-1.0.0.jar app.jar

# Now drop privileges
USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]