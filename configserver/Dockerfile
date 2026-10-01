# ---- Build stage: compile the jar with the Maven wrapper (Java 25) ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy only what dependency resolution needs first, so this layer is cached until pom.xml changes
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests

# ---- Run stage: small JRE image, non-root user ----
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --no-create-home spring
COPY --from=build /app/target/*.jar app.jar
USER spring

EXPOSE 8888
# Size the heap from the container memory limit (set in docker-compose.yml)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java", "-jar", "app.jar"]
