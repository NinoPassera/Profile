/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rabbit;

import com.google.gson.internal.LinkedTreeMap;
import com.microservice.amin.rabbit.dto.CatalogArticleResponse;
import com.microservice.amin.tools.gson.GsonTools;
import com.microservice.amin.tools.rabbit.CatalogConsumer;
import com.microservice.amin.wish.WishlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author cuent
 */
@Service
public class ArticleExistResponseConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ArticleExistResponseConsumer.class);
    private final Map<String, Boolean> responseMap = new ConcurrentHashMap<>();

    @Autowired
    private CatalogConsumer catalogConsumer;

    @Autowired
    private WishlistService wishlistService;

    @PostConstruct
    public void startListening() {
        logger.info("=== INICIANDO CONSUMER DE RESPUESTAS DEL CATÁLOGO ===");
        logger.info("Exchange: article_exist_response");
        logger.info("Routing Key: article_exist_response");
        logger.info("Queue: amin_article_exist_response");
        logger.info("=====================================================");

        catalogConsumer
                .init("article_exist_response", "article_exist_response", "amin_article_exist_response")
                .setMessageProcessor(this::processCatalogMessage)
                .start();

    }

    private void processCatalogMessage(String messageBody) {
        try {
            logger.info("Procesando respuesta del catálogo");
            logger.debug("JSON recibido del catálogo: {}", messageBody);

            // Convertir JSON directamente a CatalogArticleResponse
            CatalogArticleResponse catalogResponse = GsonTools.gson().fromJson(messageBody,
                    CatalogArticleResponse.class);

            // Validar que el objeto no sea nulo
            if (catalogResponse == null || catalogResponse.getMessage() == null) {
                logger.error("El objeto CatalogArticleResponse o su message son nulos");
                return;
            }

            // Procesar la respuesta del catálogo
            CatalogArticleResponse.ArticleExistMessage articleMessage = catalogResponse.getMessage();
            String correlationId = catalogResponse.getCorrelationId();
            boolean isValid = articleMessage.isValid();

            logger.info("Respuesta del catálogo - Artículo: {}, Válido: {}, CorrelationId: {}",
                    articleMessage.getArticleId(), isValid, correlationId);

            // Actualizar el estado del artículo en la wishlist
            wishlistService.updateArticleStatus(correlationId, isValid);

        } catch (Exception e) {
            logger.error("Error procesando la respuesta del catálogo: {}", e.getMessage(), e);
        }
    }

    public Boolean waitForResponse(String correlationId) {
        for (int i = 0; i < 15; i++) {
            if (responseMap.containsKey(correlationId)) {
                return responseMap.remove(correlationId);
            }
            try {
                Thread.sleep(1000); // Espera 1 segundo antes de volver a intentar
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null; // No se recibió respuesta
    }
}
