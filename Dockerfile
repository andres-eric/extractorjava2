# Etapa 1: Construcción (Build)
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copiar los archivos de Maven para aprovechar la caché de capas
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Asegurar permisos de ejecución para mvnw
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copiar el código fuente y construir el JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen Final (Runtime)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar el JAR desde la etapa de construcción
# El nombre del JAR debe coincidir con el artifactId y version de pom.xml
COPY --from=build /app/target/SqlExtractor-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto 8080 (puerto por defecto de Spring Boot)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
