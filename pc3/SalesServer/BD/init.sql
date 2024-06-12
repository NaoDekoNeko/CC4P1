CREATE DATABASE BD1;
\c BD1;

CREATE TABLE IF NOT EXISTS sales (
    id_sales SERIAL PRIMARY KEY,
    ruc VARCHAR(11) NOT NULL,
    name VARCHAR(100) NOT NULL,
    cost_total NUMERIC(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS details (
    id_details SERIAL PRIMARY KEY,
    id_sales INTEGER NOT NULL REFERENCES sales(id_sales),
    id_ruta INTEGER NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    name_prod VARCHAR(50) NOT NULL,
    descripcion_ruta VARCHAR(255) NOT NULL,
    lugar_de_compra VARCHAR(100) NOT NULL,
    asiento INTEGER,
    cost NUMERIC(10, 2) NOT NULL,
    total NUMERIC(10, 2) NOT NULL
);