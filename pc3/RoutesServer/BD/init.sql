IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'BD2')
BEGIN
    CREATE DATABASE BD2;
END
GO

USE BD2;
GO

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[rutas]') AND type in (N'U'))
BEGIN
    CREATE TABLE rutas (
        id_ruta INT PRIMARY KEY,
        name_ruta VARCHAR(100) NOT NULL,
        desde_lugar VARCHAR(100) NOT NULL,
        hasta_lugar VARCHAR(100) NOT NULL,
        unit INT NOT NULL,
        amount DECIMAL(10, 2) NOT NULL,
        cost DECIMAL(10, 2) NOT NULL,
        id_bus INT NOT NULL
    );
END
GO