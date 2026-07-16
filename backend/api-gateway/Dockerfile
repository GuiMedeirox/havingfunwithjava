# Dockerfile do api-gateway.
# Build multi-estágio usando o Maven Wrapper do repositório.
# A imagem final é JRE 21 (Alpine) leve.

# ---------- builder ----------
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Copia o wrapper e os POMs primeiro (cache de camadas: dependências mudam menos que código).
COPY backend/mvnw ./mvnw
COPY backend/.mvn ./.mvn
COPY backend/pom.xml ./pom.xml
COPY backend/api-gateway/pom.xml ./api-gateway/pom.xml

# O parent precisa do catalog-service para resolver o reactor multi-módulo.
COPY backend/catalog-service/pom.xml ./catalog-service/pom.xml

RUN chmod +x ./mvnw && ./mvnw -B -pl api-gateway -am dependency:go-offline

# Código-fonte do gateway.
COPY backend/api-gateway/src ./api-gateway/src

RUN ./mvnw -B -pl api-gateway -am clean package -DskipTests

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/api-gateway/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
