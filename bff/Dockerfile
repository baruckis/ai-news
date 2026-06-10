# Multi-stage image for the :bff GraphQL server.
#
# The build context must be the repository ROOT — the :bff module is part of the
# multi-module Gradle build — e.g.:
#   docker build -f bff/Dockerfile .
# deploy/docker-compose.yml already invokes it this way.

# --- Build stage: compile the fat jar from source ----------------------------
# Pinned to the same Gradle version as the repo's wrapper.
FROM gradle:9.5.1-jdk21 AS build
# The gradle image runs as the non-root `gradle` user, so chown the sources to
# let Gradle write build outputs into the tree.
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
# Use the wrapper so the Gradle version is pinned by the repo, not by the image.
RUN ./gradlew :bff:buildFatJar --no-daemon

# --- Runtime stage: minimal JRE, non-root ------------------------------------
FROM eclipse-temurin:21.0.11_10-jre
RUN useradd --system --no-create-home bff
USER bff
WORKDIR /app
COPY --from=build /home/gradle/src/bff/build/libs/bff-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
