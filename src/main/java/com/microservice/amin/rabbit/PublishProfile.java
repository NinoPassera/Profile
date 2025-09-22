/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rabbit;

import com.microservice.amin.rabbit.dto.PublishProfileDataEvent;
import com.microservice.amin.tools.rabbit.FanoutPublisher;
import com.microservice.amin.tools.rabbit.RabbitEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author cuent
 */
@Service
public class PublishProfile {
    
    @Autowired
    FanoutPublisher fanoutPublisher;
    
    public void publish(String exchange,  PublishProfileDataEvent send) {
        RabbitEvent eventToSend = new RabbitEvent();
        eventToSend.type = "profile_data";
        eventToSend.message = send;

        fanoutPublisher.publish(exchange, eventToSend);
    }
    
}
