# Etapa de construcción
FROM gradle:jdk-21-and-22-alpine as build

# Copiar los archivos necesarios para la compilación
COPY /app/ /app
COPY /gradlew /gradlew
COPY /gradlew.bat /gradlew.bat
COPY /gradle /gradle
COPY /settings.gradle /settings.gradle
COPY /app/build.gradle /app/build.gradle
WORKDIR /app

# Dar permisos de ejecución al wrapper de Gradle
RUN chmod +x /gradlew

# Compilar el proyecto
RUN ./gradlew build

# Configurar la base de datos y RabbitMQ como variables de entorno
ENV RABBITMQ_HOST=rabbitmq
ENV RABBITMQ_PORT=5672
ENV POSTGRES_HOST=sales-db
ENV POSTGRES_PORT=5432
ENV POSTGRES_DB=BD1
ENV POSTGRES_USER=admin
ENV POSTGRES_PASSWORD=admin

# Ejecutar la aplicación
CMD ["./gradlew", "run"]