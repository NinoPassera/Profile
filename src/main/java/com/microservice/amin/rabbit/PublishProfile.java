/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rabbit;

import com.microservice.amin.rabbit.dto.PublishProfileDataEvent;
import com.microservice.amin.tools.rabbit.RabbitPublisher;
import org.springframework.stereotype.Service;

/**
 *
 * @author cuent
 */
@Service
public class PublishProfile extends RabbitPublisher {

    @Override
    protected String getExchangeType() {
        return "fanout";
    }

    public void publish(String exchange, PublishProfileDataEvent send) {
        // Publicar directamente el DTO sin envolver en RabbitEvent
        super.publish(exchange, "", send);
    }

}
