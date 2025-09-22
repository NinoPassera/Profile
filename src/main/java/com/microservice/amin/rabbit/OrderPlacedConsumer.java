package com.microservice.amin.rabbit;

import com.microservice.amin.rabbit.dto.OrderPlacedEvent;
import com.microservice.amin.tools.gson.GsonTools;
import com.microservice.amin.tools.rabbit.RabbitConsumer;
import com.microservice.amin.wish.WishlistService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Consumer para procesar eventos order_placed del microservicio Orders
 * 
 * Funcionalidad:
 * - Escucha el evento order_placed del exchange order_placed (fanout)
 * - Procesa el mensaje para obtener userId y artículos comprados
 * - Elimina los artículos comprados de la wishlist del usuario
 * - Maneja casos donde no hay wishlist o no hay coincidencias
 */
@Component
public class OrderPlacedConsumer extends RabbitConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderPlacedConsumer.class);

    @Autowired
    private WishlistService wishlistService;

    /**
     * Inicialización automática del consumer al arrancar la aplicación
     */
    @PostConstruct
    public void initOrderPlacedConsumer() {
        logger.info("=== INICIANDO CONSUMER DE EVENTOS ORDER_PLACED ===");

        // Configuración para exchange fanout order_placed
        String exchange = "order_placed";
        String queue = "profile_order_placed_queue"; // Cola específica para este microservicio

        logger.info("Exchange: {}", exchange);
        logger.info("Queue: {}", queue);
        logger.info("Exchange Type: fanout");
        logger.info("=================================================");

        this.initFanout(exchange, queue);
        this.setMessageProcessor(this::processOrderPlacedEvent);
        this.start();

        logger.info("OrderPlacedConsumer inicializado exitosamente");
    }

    /**
     * Procesa el mensaje del evento order_placed
     */
    private void processOrderPlacedEvent(String messageBody) {
        try {
            logger.info("Procesando evento order_placed: {}", messageBody);

            OrderPlacedEvent orderEvent = null;

            // Primero intenta deserializar directamente
            try {
                orderEvent = GsonTools.fromJson(messageBody, OrderPlacedEvent.class);
            } catch (Exception e) {
                logger.debug("Error deserializando directamente, intentando estructura anidada: {}", e.getMessage());
            }

            // Si falla, intenta con estructura anidada (wrapper con correlation_id,
            // message, etc.)
            if (orderEvent == null || orderEvent.getUserId() == null) {
                try {
                    // Deserializa el wrapper
                    com.google.gson.JsonObject wrapper = GsonTools.fromJson(messageBody,
                            com.google.gson.JsonObject.class);

                    if (wrapper != null && wrapper.has("message")) {
                        String innerMessage = wrapper.get("message").toString();
                        orderEvent = GsonTools.fromJson(innerMessage, OrderPlacedEvent.class);
                        logger.debug("Deserializado exitosamente desde estructura anidada");
                    }
                } catch (Exception e) {
                    logger.error("Error deserializando estructura anidada: {}", e.getMessage());
                }
            }

            if (orderEvent == null) {
                logger.error("Error: No se pudo deserializar el mensaje order_placed en ningún formato");
                return;
            }

            // Validaciones básicas
            if (orderEvent.getUserId() == null || orderEvent.getUserId().trim().isEmpty()) {
                logger.warn("UserId nulo o vacío en evento order_placed, ignorando mensaje");
                return;
            }

            if (orderEvent.getArticles() == null || orderEvent.getArticles().isEmpty()) {
                logger.warn("Lista de artículos vacía en evento order_placed para userId: {}, ignorando mensaje",
                        orderEvent.getUserId());
                return;
            }

            // Extrae los IDs de los artículos comprados
            List<String> purchasedArticleIds = orderEvent.getArticles().stream()
                    .map(OrderPlacedEvent.ArticleOrder::getArticleId)
                    .filter(articleId -> articleId != null && !articleId.trim().isEmpty())
                    .collect(Collectors.toList());

            if (purchasedArticleIds.isEmpty()) {
                logger.warn("No se encontraron articleIds válidos en evento order_placed para userId: {}",
                        orderEvent.getUserId());
                return;
            }

            logger.info("Procesando orden {} para userId: {} con {} artículos",
                    orderEvent.getOrderId(), orderEvent.getUserId(), purchasedArticleIds.size());

            logger.info("Datos del evento - OrderId: {}, CartId: {}, UserId: {}, Articles: {}",
                    orderEvent.getOrderId(), orderEvent.getCartId(), orderEvent.getUserId(),
                    orderEvent.getArticles());

            // Elimina los artículos comprados de la wishlist
            int removedCount = wishlistService.removePurchasedArticlesFromWishlist(
                    orderEvent.getUserId(),
                    purchasedArticleIds);

            logger.info("Evento order_placed procesado exitosamente - Orden: {}, UserId: {}, " +
                    "Artículos procesados: {}, Artículos eliminados de wishlist: {}",
                    orderEvent.getOrderId(), orderEvent.getUserId(),
                    purchasedArticleIds.size(), removedCount);

        } catch (Exception e) {
            logger.error("Error procesando evento order_placed: {}", e.getMessage(), e);
        }
    }

    /**
     * Crea el consumer específico para RabbitMQ
     */
    @Override
    protected DefaultConsumer createMessageConsumer(Channel channel) {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                    AMQP.BasicProperties properties, byte[] body) throws IOException {

                try {
                    String messageBody = new String(body, "UTF-8");
                    logger.debug("Mensaje recibido en OrderPlacedConsumer: {}", messageBody);

                    // Procesa el mensaje
                    processOrderPlacedEvent(messageBody);

                    // Confirma el procesamiento del mensaje
                    channel.basicAck(envelope.getDeliveryTag(), false);

                } catch (Exception e) {
                    logger.error("Error manejando delivery en OrderPlacedConsumer: {}", e.getMessage(), e);

                    try {
                        // Rechaza el mensaje y lo reencola
                        channel.basicNack(envelope.getDeliveryTag(), false, true);
                    } catch (IOException ioException) {
                        logger.error("Error rechazando mensaje: {}", ioException.getMessage(), ioException);
                    }
                }
            }
        };
    }
}
