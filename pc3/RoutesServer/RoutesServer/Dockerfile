FROM python:3.11-slim as app

# Instalar dependencias
RUN apt-get update && apt-get install -y unixodbc-dev curl gnupg && \
    curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add - && \
    curl https://packages.microsoft.com/config/debian/10/prod.list > /etc/apt/sources.list.d/mssql-release.list && \
    apt-get update && ACCEPT_EULA=Y apt-get install -y msodbcsql17

WORKDIR /app
COPY app/ /app/

RUN pip install -r requirements.txt

CMD ["python", "route_server.py"]
