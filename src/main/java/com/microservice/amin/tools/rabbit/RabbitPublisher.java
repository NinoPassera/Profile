package com.microservice.amin.tools.rabbit;

import com.microservice.amin.config.AppConfig;
import com.microservice.amin.tools.gson.GsonTools;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Clase padre abstracta para todos los publishers de RabbitMQ
 * Maneja la conexión, logs y errores de forma unificada
 */
@Service
public abstract class RabbitPublisher {

    protected static final Logger logger = LoggerFactory.getLogger(RabbitPublisher.class);

    @Autowired
    protected AppConfig appConfig;

    /**
     * Publica un mensaje en RabbitMQ
     */
    public void publish(String exchange, String routingKey, Object message) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(appConfig.getRabbitHost());
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // Declarar exchange
            channel.exchangeDeclare(exchange, getExchangeType());

            String jsonMessage = GsonTools.toJson(message);
            logPublishInfo(exchange, routingKey, jsonMessage);

            channel.basicPublish(exchange, routingKey, null, jsonMessage.getBytes());

            logger.info("✅ Mensaje publicado exitosamente en RabbitMQ");

            channel.close();
            connection.close();

        } catch (Exception e) {
            logger.error("❌ Error al publicar mensaje: {}", e.getMessage(), e);
        }
    }

    /**
     * Obtiene el tipo de exchange (direct, fanout, etc.)
     */
    protected abstract String getExchangeType();

    /**
     * Log de información de publicación
     */
    protected void logPublishInfo(String exchange, String routingKey, String jsonMessage) {
        logger.info("=== PUBLICANDO EN RABBITMQ ===");
        logger.info("Host: {}", appConfig.getRabbitHost());
        logger.info("Exchange: {}", exchange);
        logger.info("Routing Key: {}", routingKey);
        logger.info("Mensaje JSON: {}", jsonMessage);
        logger.info("=============================");
    }
}
