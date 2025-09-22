/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rabbit;

import com.microservice.amin.rabbit.dto.WishItemDataEvent;
import com.microservice.amin.tools.rabbit.DirectPublisher;
import com.microservice.amin.tools.rabbit.RabbitEvent;

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
        // Crear el cuerpo del mensaje usando WishItemDataEvent
        WishItemDataEvent message = new WishItemDataEvent(articleId, correlationId);

        // Crear el evento RabbitEvent
        RabbitEvent event = new RabbitEvent();
        event.type = "article_exist";
        event.version = 1;
        event.exchange = "article_exist";
        event.routingKey = "article_exist";
        event.message = message;

        // Publicar el evento
        directPublisher.publish(event.exchange, event.routingKey, event);
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