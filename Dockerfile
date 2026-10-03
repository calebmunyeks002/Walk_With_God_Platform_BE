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

# Non-root user for security
RUN useradd --system --uid 1001 appuser
USER appuser

COPY --from=build /app/target/walk-with-god-api-1.0.0.jar app.jar

# Media storage directory (ephemeral on Render free tier)
RUN mkdir -p /app/uploads

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]