# Multi-stage build for the AICTE Spring Boot backend
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN useradd --system --create-home --shell /usr/sbin/nologin spring
COPY --from=build /workspace/target/*.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080
USER spring

# Render provides PORT at runtime; Spring Boot must bind to 0.0.0.0.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.address=0.0.0.0 -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
