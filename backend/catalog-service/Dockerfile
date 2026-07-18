# Dockerfile do catalog-service (multi-stage).
# Build com Maven Wrapper → runtime JRE 21 leve.
# Copia TODOS os POMs dos módulos porque o parent declara todos no <modules>.

# ---------- builder ----------
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Wrapper + TODOS os POMs (o reactor precisa resolver o parent inteiro).
COPY backend/mvnw ./mvnw
COPY backend/.mvn ./.mvn
COPY backend/pom.xml ./pom.xml
COPY backend/catalog-service/pom.xml ./catalog-service/pom.xml
COPY backend/orders-service/pom.xml ./orders-service/pom.xml
COPY backend/payment-service/pom.xml ./payment-service/pom.xml
COPY backend/api-gateway/pom.xml ./api-gateway/pom.xml

RUN chmod +x ./mvnw && ./mvnw -B -pl catalog-service -am dependency:go-offline

# Código-fonte.
COPY backend/catalog-service/src ./catalog-service/src

RUN ./mvnw -B -pl catalog-service -am clean package -DskipTests

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/catalog-service/target/*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
