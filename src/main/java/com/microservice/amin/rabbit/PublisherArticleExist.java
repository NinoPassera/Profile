/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rabbit;

import com.microservice.amin.rabbit.dto.ArticleExistRequest;
import com.microservice.amin.tools.rabbit.RabbitPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 *
 * @author cuent
 */
@Component
public class PublisherArticleExist extends RabbitPublisher {
    private static final Logger logger = LoggerFactory.getLogger(PublisherArticleExist.class);
    private final Map<String, Boolean> responseMap = new ConcurrentHashMap<>();

    @Override
    protected String getExchangeType() {
        return "direct";
    }

    public void publishArticleExist(String articleId, String correlationId) {
        // Crear el mensaje usando DTO
        ArticleExistRequest.ArticleExistMessage message = new ArticleExistRequest.ArticleExistMessage(correlationId,
                articleId);
        ArticleExistRequest catalogMessage = new ArticleExistRequest(
                correlationId,
                "article_exist_response",
                "article_exist_response",
                message);

        logger.info("=== ENVIANDO MENSAJE AL CATÁLOGO ===");
        logger.info("Exchange: article_exist");
        logger.info("Routing Key: article_exist");
        logger.info("Correlation ID: {}", correlationId);
        logger.info("Article ID: {}", articleId);
        logger.info("Mensaje completo: {}", catalogMessage);
        logger.info("=====================================");

        // Publicar usando DTO
        publish("article_exist", "article_exist", catalogMessage);
    }

    public boolean waitForResponse(String articleId, long timeout) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            if (responseMap.containsKey(articleId)) {
                return responseMap.remove(articleId);
            }
            Thread.sleep(100); // Espera un poco antes de volver a intentar
        }
        throw new InterruptedException("Timeout esperando la respuesta del consumidor.");
    }

    public void handleResponse(String articleId, boolean isValid) {
        responseMap.put(articleId, isValid);
    }
}