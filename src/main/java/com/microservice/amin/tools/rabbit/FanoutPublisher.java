
package com.microservice.amin.tools.rabbit;



import com.microservice.amin.tools.EnvironmentVars;
import com.microservice.amin.tools.gson.GsonTools;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Publicar en una cola direct es enviar un mensaje directo a un destinatario en particular,
 * Necesitamos un exchange y un queue especifico para enviar correctamente el mensaje.
 * Tanto el consumer como el publisher deben compartir estos mismos datos.
 */
@Service
public class FanoutPublisher {
    //@Autowired
    //CatalogLogger logger;

    @Autowired
    EnvironmentVars environmentVars;

    public void publish(String exchange, RabbitEvent message) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(environmentVars.envData.rabbitServerUrl);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(exchange, "fanout");
            channel.basicPublish(exchange, "", null, GsonTools.toJson(message).getBytes());





            
        } catch (Exception e) {
            //logger.error("RabbitMQ no se pudo encolar " + message.type, e);
        }
    }
}