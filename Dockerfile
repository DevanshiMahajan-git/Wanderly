FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 10001 wanderly
COPY --from=build /workspace/target/wanderly.jar app.jar
RUN mkdir -p /app/data && chown -R wanderly:wanderly /app
USER wanderly
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
