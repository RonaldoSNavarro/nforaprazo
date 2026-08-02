# ============================================
# Stage 1: Build com Maven
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar POM primeiro para cache de dependências
COPY pom.xml .

# Baixar dependências (cached se pom.xml não mudar)
RUN mvn dependency:go-offline -B

# Copiar código-fonte e compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================
# Stage 2: Runtime mínimo
# ============================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Criar diretório de uploads
RUN mkdir -p /app/uploads

# Copiar o JAR do stage de build
COPY --from=builder /app/target/*.jar app.jar

# Porta padrão do Spring Boot
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8080/login || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
