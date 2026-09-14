FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY . .

RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
