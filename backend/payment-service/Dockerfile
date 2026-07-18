# Dockerfile do payment-service (multi-stage).
# Build com Maven Wrapper → runtime JRE 21 leve.

# ---------- builder ----------
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Wrapper + POMs (o reactor precisa do parent + todos os módulos para resolver).
COPY backend/mvnw ./mvnw
COPY backend/.mvn ./.mvn
COPY backend/pom.xml ./pom.xml
COPY backend/catalog-service/pom.xml ./catalog-service/pom.xml
COPY backend/orders-service/pom.xml ./orders-service/pom.xml
COPY backend/payment-service/pom.xml ./payment-service/pom.xml

RUN chmod +x ./mvnw && ./mvnw -B -pl payment-service -am dependency:go-offline

# Código-fonte.
COPY backend/payment-service/src ./payment-service/src

RUN ./mvnw -B -pl payment-service -am clean package -DskipTests

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/payment-service/target/*.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
