FROM maven:3.8.6-eclipse-temurin-17-alpine AS builder
COPY ./pom.xml /Sources/
WORKDIR /Sources
RUN mvn dependency:go-offline

COPY ./src/ /Sources/src/
RUN mvn package -DskipTests


FROM eclipse-temurin:17.0.3_7-jre-alpine

# Get DXVK Cache Tool and make it executable
ADD https://github.com/DarkTigrus/dxvk-cache-tool/releases/download/v1.1.2/dxvk-cache-tool /bin/dxvk-cache-tool
RUN chmod +x /bin/dxvk-cache-tool

COPY --from=builder /Sources/target/*.jar /App/app.jar
WORKDIR /App

EXPOSE 8080
CMD java -jar app.jar --spring.profiles.active=prod
