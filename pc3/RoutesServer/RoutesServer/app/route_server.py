import pyodbc
import pika
import logging
import time
import uuid
import contextlib

# Configuración de la base de datos
conn_str = (
    r'DRIVER={ODBC Driver 17 for SQL Server};'
    r'SERVER=routes-db;'
    r'DATABASE=BD2;'
    r'UID=sa;'
    r'PWD=Admin@123;'
    r'Connection Timeout=30;'
)

# Configuración de RabbitMQ
rabbitmq_host = 'localhost'
rabbitmq_port = 5672
rabbitmq_queue = 'python-java-queue'

# Configuración del logging
logging.basicConfig(level=logging.INFO)

# Clase Producto
class Product:
    def __init__(self, ID, Name, Category, Amount, Cost):
        self.ID = ID
        self.Name = Name
        self.Category = Category
        self.Amount = Amount
        self.Cost = Cost
        self.CostTotal = self.Cost * self.Amount

def products_to_string(products):
    msg = ""
    for product in products:
        msg += f"{product.ID},{product.Name},{product.Category},{product.Amount},{product.Cost},{product.CostTotal};"
    return msg[:-1]

def send_products(cursor):
    cursor.execute("SELECT ID_PROD, NAME_PROD, COST FROM products")
    rows = cursor.fetchall()
    msg = ""
    for row in rows:
        msg += f"{row.ID_PROD},{row.NAME_PROD},{row.COST};"
    return msg[:-1]

def check_existence(cursor, id, amount):
    cursor.execute("SELECT AMOUNT FROM products WHERE ID_PROD = ?", id)
    row = cursor.fetchone()
    return row and row.AMOUNT >= amount

def check_all_existences(cursor, values):
    counter = 0
    for value in values:
        prod = value.split(",")
        id = int(prod[0])
        amount = int(prod[1])
        if check_existence(cursor, id, amount):
            counter += 1
    return counter == len(values)

def update_products(cursor, values):
    for value in values:
        prod = value.split(",")
        id = int(prod[0])
        amount = int(prod[1])
        cursor.execute("UPDATE products SET AMOUNT = AMOUNT - ? WHERE ID_PROD = ?", amount, id)

def get_data_from_id(cursor, id, amount):
    cursor.execute("SELECT ID_PROD, NAME_PROD, CATEGORY, COST FROM products WHERE ID_PROD = ?", id)
    row = cursor.fetchone()
    if row:
        product = Product(row.ID_PROD, row.NAME_PROD, row.CATEGORY, amount, row.COST)
        return product
    return None

def send_to_java(cursor, name, ruc, products, channel):
    msg = f"{name};{ruc}"
    temp = products.split(",")
    prds = []
    for i in range(0, len(temp), 2):
        id = int(temp[i].strip())
        amount = int(temp[i + 1].strip())
        prd = get_data_from_id(cursor, id, amount)
        if prd:
            prd.CostTotal = prd.Cost * prd.Amount
            prd.CostTotal = round(prd.CostTotal, 2)
            prds.append(prd)
    msg += f";{products_to_string(prds)}"
    channel.basic_publish(
        exchange='',
        routing_key='go-java-queue',
        body=msg,
        properties=pika.BasicProperties(
            content_type='text/plain'
        )
    )
    logging.info("Venta Realizada")
    return "Venta Realizada"

def on_request(ch, method, props, body, cursor, channel_java):
    msg = body.decode()
    logging.info(f" [.] Recibido: {msg}")
    response = ""
    if msg == "get_products":
        response = send_products(cursor)
    else:
        resp = msg.split(";")
        values = resp[2].split("/")
        if not check_all_existences(cursor, values):
            response = "No se pudo realizar la venta"
        else:
            update_products(cursor, values)
            send_to_java(cursor, resp[0], resp[1], ",".join(values), channel_java)
            response = "Venta Realizada"

    ch.basic_publish(
        exchange='',
        routing_key=props.reply_to,
        properties=pika.BasicProperties(
            correlation_id=props.correlation_id
        ),
        body=response
    )
    ch.basic_ack(delivery_tag=method.delivery_tag)

def main():
    max_retries = 10
    retry_delay = 10

    for i in range(max_retries):
        try:
            with contextlib.closing(pyodbc.connect(conn_str)) as conn:
                break
        except pyodbc.Error as e:
            if i < max_retries - 1:
                logging.error(f"Failed to connect to database, retrying in {retry_delay} seconds...")
                time.sleep(retry_delay)
            else:
                raise

    cursor = conn.cursor()
    connection = pika.BlockingConnection(pika.ConnectionParameters(host=rabbitmq_host))
    channel = connection.channel()

    channel.queue_declare(queue=rabbitmq_queue)
    channel.basic_qos(prefetch_count=1)

    channel_java = connection.channel()

    channel.basic_consume(
        queue=rabbitmq_queue,
        on_message_callback=lambda ch, method, props, body: on_request(ch, method, props, body, cursor, channel_java)
    )

    logging.info(" [*] Awaiting RPC requests")
    channel.start_consuming()

if __name__ == '__main__':
    main()
