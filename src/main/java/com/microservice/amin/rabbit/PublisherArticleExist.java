/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rabbit;

import com.microservice.amin.tools.rabbit.DirectPublisher;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

/**
 *
 * @author cuent
 */
@Component
public class PublisherArticleExist {
    private final Map<String, Boolean> responseMap = new ConcurrentHashMap<>();

    @Autowired
    private DirectPublisher directPublisher;

    public void publishArticleExist(String articleId, String correlationId) {
        // Crear el mensaje que espera el catálogo
        Map<String, Object> message = new HashMap<>();
        message.put("referenceId", correlationId);
        message.put("articleId", articleId);

        // Crear el objeto principal que espera el catálogo
        Map<String, Object> catalogMessage = new HashMap<>();
        catalogMessage.put("correlation_id", correlationId);
        catalogMessage.put("routing_key", "article_exist_response");
        catalogMessage.put("exchange", "article_exist_response");
        catalogMessage.put("message", message);

        System.out.println("=== ENVIANDO MENSAJE AL CATÁLOGO ===");
        System.out.println("Exchange: article_exist");
        System.out.println("Routing Key: article_exist");
        System.out.println("Correlation ID: " + correlationId);
        System.out.println("Article ID: " + articleId);
        System.out.println("Mensaje completo: " + catalogMessage);
        System.out.println("=====================================");

        // Publicar directamente el mensaje (no usar RabbitEvent)
        directPublisher.publish("article_exist", "article_exist", catalogMessage);
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