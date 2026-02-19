FROM eclipse-temurin:17-jre
WORKDIR /app

# Install curl for healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

ARG MODULE
# Copy the pre-built JAR from the host's module target directory
COPY ${MODULE}/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
