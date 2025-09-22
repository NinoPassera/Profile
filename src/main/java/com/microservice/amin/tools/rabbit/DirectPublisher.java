/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.tools.rabbit;

import com.microservice.amin.tools.EnvironmentVars;
import com.microservice.amin.tools.gson.GsonTools;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author cuent
 */
@Service
public class DirectPublisher {
    
    @Autowired
    EnvironmentVars environmentVars;

    public void publish(String exchange, String routingKey, RabbitEvent message) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(environmentVars.envData.rabbitServerUrl);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchange, "direct");
            
            
            channel.basicPublish(exchange, routingKey, null, GsonTools.toJson(message).getBytes());

            
        } catch (Exception e) {
            System.out.println("Eror");
        }
    }
    
    
}
