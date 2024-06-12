package salesserver;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

public class Rabbit {
    private final static String QUEUE_NAME = "python-java-queue";
    private Server server;

    public void setServer(Server server) {
        this.server = server;
    }

    public void run() throws IOException, TimeoutException {
        System.out.println("Esperando mensajes desde el canal '" + QUEUE_NAME + "'");
        initChannel(QUEUE_NAME);
    }

    void initChannel(String queueName) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setVirtualHost("sales_host");
        factory.setUsername("admin");
        factory.setPassword("admin");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queueName, false, false, false, null);
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(
                    String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws UnsupportedEncodingException {
                String message = new String(body, "UTF-8");
                System.out.println("Mensaje recibido desde el canal '" + QUEUE_NAME + "': '" + message + "'");
                try {
                    server.parseData(message);
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println("Error al procesar el mensaje: " + e.getMessage());
                }
            }
        };

        channel.basicConsume(QUEUE_NAME, true, consumer);
    }
}