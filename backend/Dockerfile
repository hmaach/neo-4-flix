# One recipe for every backend module. Compose passes SERVICE (module name) and PORT.
# Maven runs inside the build stage, so the auditor only needs Docker.

FROM maven:3.9.16-eclipse-temurin-21 AS build
ARG SERVICE
WORKDIR /build
COPY . .
# The cache mount keeps the Maven repository between the builds of all services: dependencies download once.
# Tests run on the host with ./mvnw verify, not in every image build.
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -pl ${SERVICE} -am -DskipTests package

FROM eclipse-temurin:21.0.13_9-jre-noble
ARG SERVICE
ARG PORT
RUN groupadd --system --gid 1001 app && useradd --system --uid 1001 --gid app app
WORKDIR /app
COPY --from=build /build/${SERVICE}/target/${SERVICE}.jar app.jar
USER app
ENV PORT=${PORT}
# Heap sized from the container mem_limit instead of the host RAM.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=10 \
  CMD curl -fs http://localhost:${PORT}/actuator/health || exit 1
EXPOSE ${PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
