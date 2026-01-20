FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

# Create database directory and set permissions
RUN mkdir -p /data/database && \
    chmod 755 /data/database

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]



