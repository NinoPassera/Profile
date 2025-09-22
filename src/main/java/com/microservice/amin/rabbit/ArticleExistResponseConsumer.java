/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rabbit;

import com.google.gson.internal.LinkedTreeMap;
import com.microservice.amin.rabbit.dto.SendArticleExist;
import com.microservice.amin.tools.gson.GsonTools;
import com.microservice.amin.tools.rabbit.DirectConsumer;
import com.microservice.amin.tools.rabbit.RabbitEvent;
import com.microservice.amin.wish.WishlistService;
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

    private final Map<String, Boolean> responseMap = new ConcurrentHashMap<>();

    @Autowired
    private DirectConsumer directConsumer;

    @Autowired
    private WishlistService wishlistService;

    @PostConstruct
    public void startListening() {
        directConsumer
                .init("article_exist", "article_exist", "catalog_article_exist")
                .addProcessor("article_exist", this::processResponse)
                .start();

    }

    private void processResponse(RabbitEvent event) {
        try {

            SendArticleExist sendArticleExist = null;
            System.out.println("eventType:   " + event.type);
            
            if ("article_exist".equals(event.type)) {
                

                // Convertir 'message' en SendArticleExist
                if (event.message instanceof LinkedTreeMap) {
                    String jsonMessage = GsonTools.toJson(event.message);
                    System.out.println("JSON generado desde LinkedTreeMap: " + jsonMessage);

                    // Convertir JSON a objeto
                    sendArticleExist = GsonTools.gson().fromJson(jsonMessage, SendArticleExist.class);

                    // Validar que el objeto no sea nulo
                    if (sendArticleExist == null || sendArticleExist.getReferenceId() == null) {
                        System.err.println("El objeto SendArticleExist o su referenceId son nulos");
                        return;
                    }
                } else {
                    System.err.println("Formato inesperado para 'message': " + event.message);
                    return;
                }
            } else {
                throw new Error("No se pudo procesar el mensaje, tipo desconocido: " + event.type);
            }

            wishlistService.updateArticleStatus(sendArticleExist.getReferenceId(), true);
            
        } catch (Exception e) {
            System.err.println("Error procesando la respuesta: " + e.getMessage());
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
