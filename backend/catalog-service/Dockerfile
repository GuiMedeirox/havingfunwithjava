# Dockerfile do catalog-service (multi-stage, issue de dockerização).
# Build com Maven Wrapper → runtime JRE 21 leve.

# ---------- builder ----------
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Wrapper + POMs (cache de camadas: dependências mudam menos que código).
COPY backend/mvnw ./mvnw
COPY backend/.mvn ./.mvn
COPY backend/pom.xml ./pom.xml
COPY backend/catalog-service/pom.xml ./catalog-service/pom.xml

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
