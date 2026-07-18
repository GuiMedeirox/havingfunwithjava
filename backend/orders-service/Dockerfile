# Dockerfile do orders-service (multi-stage).
# Copia TODOS os POMs dos módulos porque o parent declara todos no <modules>.

# ---------- builder ----------
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

COPY backend/mvnw ./mvnw
COPY backend/.mvn ./.mvn
COPY backend/pom.xml ./pom.xml
COPY backend/catalog-service/pom.xml ./catalog-service/pom.xml
COPY backend/orders-service/pom.xml ./orders-service/pom.xml
COPY backend/payment-service/pom.xml ./payment-service/pom.xml
COPY backend/api-gateway/pom.xml ./api-gateway/pom.xml

RUN chmod +x ./mvnw && ./mvnw -B -pl orders-service -am dependency:go-offline

COPY backend/orders-service/src ./orders-service/src

RUN ./mvnw -B -pl orders-service -am clean package -DskipTests

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/orders-service/target/*.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
