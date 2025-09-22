# Usa una imagen base de OpenJDK
FROM openjdk:23-jdk-slim

# Establece el directorio de trabajo en el contenedor
WORKDIR /app

# Copia el archivo JAR generado por Maven a la imagen
COPY target/spring-boot-docker.jar app.jar

# Expone el puerto de tu aplicación (ajusta según tu configuración)
EXPOSE 8080

# Define el comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
