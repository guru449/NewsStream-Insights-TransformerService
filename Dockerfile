# Use Java 17 JDK for building
FROM openjdk:17-oracle as build
WORKDIR /app
COPY . /app
# Run Gradle build
RUN ./gradlew clean build -x test  # Assumes you have a gradlew in your project

# Use Java 17 JRE for running
FROM openjdk:17-oracle
WORKDIR /app
# Copy the built JAR from the build/libs directory
COPY --from=build /app/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]

# After your existing commands in Dockerfile
USER root
