FROM mcr.microsoft.com/mssql/server:2019-latest

# Establecer variables de entorno para SQL Server
ENV SA_PASSWORD=Admin@123
ENV ACCEPT_EULA=Y
ENV MSSQL_PID=Express

# Copiar el script de inicialización de la base de datos
COPY init.sql /init.sql

CMD /bin/bash -c '/opt/mssql/bin/sqlservr & sleep 30 && /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P $SA_PASSWORD -d master -i /init.sql && tail -f /dev/null'