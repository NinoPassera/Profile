package com.microservice.amin.tools.rabbit;

import com.microservice.amin.tools.EnvironmentVars;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * Consumer específico para mensajes del catálogo que no tienen campo 'type'
 */
@Service
public class CatalogConsumer {

    @Autowired
    EnvironmentVars environmentVars;

    private String exchange;
    private String routingKey;
    private String queue;
    private Consumer<String> messageProcessor;

    public CatalogConsumer init(String exchange, String routingKey, String queue) {
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.queue = queue;
        return this;
    }

    public CatalogConsumer setMessageProcessor(Consumer<String> processor) {
        this.messageProcessor = processor;
        return this;
    }

    /**
     * Conecta a rabbit para escuchar eventos del catálogo
     */
    public void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(environmentVars.envData.rabbitServerUrl);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchange, "direct");
            channel.queueDeclare(queue, false, false, false, null);
            channel.queueBind(queue, exchange, routingKey);

            System.out.println("=== INICIANDO CATALOG CONSUMER ===");
            System.out.println("Exchange: " + exchange);
            System.out.println("Routing Key: " + routingKey);
            System.out.println("Queue: " + queue);
            System.out.println("=================================");

            new Thread(() -> {
                try {
                    channel.basicConsume(queue, true, new CatalogMessageConsumer(channel));
                } catch (Exception e) {
                    System.err.println("Error en CatalogConsumer: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            System.err.println("Error iniciando CatalogConsumer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    class CatalogMessageConsumer extends DefaultConsumer {

        CatalogMessageConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                byte[] body) {
            try {
                String messageBody = new String(body);
                System.out.println("=== MENSAJE RECIBIDO DEL CATÁLOGO ===");
                System.out.println("Body: " + messageBody);
                System.out.println("====================================");

                if (messageProcessor != null) {
                    messageProcessor.accept(messageBody);
                }

            } catch (Exception e) {
                System.err.println("Error procesando mensaje del catálogo: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
