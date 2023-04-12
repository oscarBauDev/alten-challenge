FROM openjdk:11-jdk-slim as builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .
RUN ./mvnw dependency:go-offline
COPY src/ src/
RUN ./mvnw package -DskipTests
RUN rm -rf /root/.m2 && rm -rf /app/src

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/target/booking-app-0.0.1-SNAPSHOT.jar .
EXPOSE 8080
CMD ["java", "-jar", "booking-app-0.0.1-SNAPSHOT.jar"]
