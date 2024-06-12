FROM postgres:alpine

# Establecer variables de entorno para PostgreSQL
ENV POSTGRES_DB=BD1
ENV POSTGRES_USER=admin
ENV POSTGRES_PASSWORD=admin

# Copiar el script de inicialización de la base de datos
COPY init.sql /docker-entrypoint-initdb.d/