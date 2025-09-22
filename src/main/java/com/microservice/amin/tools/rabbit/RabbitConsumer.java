package com.microservice.amin.tools.rabbit;

import com.microservice.amin.config.AppConfig;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * Clase padre abstracta para todos los consumers de RabbitMQ
 * Maneja la conexión, logs y errores de forma unificada
 */
@Service
public abstract class RabbitConsumer {

    protected static final Logger logger = LoggerFactory.getLogger(RabbitConsumer.class);

    @Autowired
    protected AppConfig appConfig;

    protected String exchange;
    protected String routingKey;
    protected String queue;
    protected String exchangeType = "direct";
    protected Consumer<String> messageProcessor;

    /**
     * Inicializa el consumer para exchanges direct
     */
    public RabbitConsumer init(String exchange, String routingKey, String queue) {
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.queue = queue;
        this.exchangeType = "direct";
        return this;
    }

    /**
     * Inicializa el consumer para exchanges fanout
     */
    public RabbitConsumer initFanout(String exchange, String queue) {
        this.exchange = exchange;
        this.routingKey = ""; // Fanout no usa routing key
        this.queue = queue;
        this.exchangeType = "fanout";
        return this;
    }

    /**
     * Establece el procesador de mensajes
     */
    public RabbitConsumer setMessageProcessor(Consumer<String> processor) {
        this.messageProcessor = processor;
        return this;
    }

    /**
     * Conecta a rabbit para escuchar eventos
     */
    public void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(appConfig.getRabbitHost());
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // Declarar exchange
            channel.exchangeDeclare(exchange, exchangeType);

            // Declarar cola
            if (exchangeType.equals("fanout") && (queue == null || queue.isEmpty())) {
                queue = channel.queueDeclare().getQueue();
            } else {
                channel.queueDeclare(queue, false, false, false, null);
            }

            // Enlazar cola al exchange
            if (exchangeType.equals("fanout")) {
                channel.queueBind(queue, exchange, "");
            } else {
                channel.queueBind(queue, exchange, routingKey);
            }

            logConnectionInfo();

            new Thread(() -> {
                try {
                    channel.basicConsume(queue, true, createMessageConsumer(channel));
                } catch (Exception e) {
                    logger.error("Error en RabbitConsumer: {}", e.getMessage(), e);
                }
            }).start();
        } catch (Exception e) {
            logger.error("Error iniciando RabbitConsumer: {}", e.getMessage(), e);
        }
    }

    /**
     * Crea el consumer específico para cada tipo
     */
    protected abstract DefaultConsumer createMessageConsumer(Channel channel);

    /**
     * Procesa el mensaje recibido
     */
    protected void processMessage(String messageBody) {
        try {
            logger.debug("Mensaje recibido: {}", messageBody);
            if (messageProcessor != null) {
                messageProcessor.accept(messageBody);
            }
        } catch (Exception e) {
            logger.error("Error procesando mensaje: {}", e.getMessage(), e);
        }
    }

    /**
     * Log de información de conexión
     */
    protected void logConnectionInfo() {
        logger.info("=== INICIANDO RABBIT CONSUMER ===");
        logger.info("Exchange: {} (tipo: {})", exchange, exchangeType);
        logger.info("Routing Key: {}", routingKey);
        logger.info("Queue: {}", queue);
        logger.info("=================================");
    }
}
