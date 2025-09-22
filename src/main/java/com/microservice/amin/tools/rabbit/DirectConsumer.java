/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.tools.rabbit;

import com.microservice.amin.server.ValidatorService;
import com.microservice.amin.tools.EnvironmentVars;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Escuchar en una cola direct es recibir un mensaje directo, Necesitamos un
 * exchange y un queue especifico para enviar correctamente el mensaje. Tanto el
 * consumer como el publisher deben compartir estos mismos datos.
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DirectConsumer {

    @Autowired
    EnvironmentVars environmentVars;

    @Autowired
    ValidatorService validator;

    private String exchange;
    private String routinghKey;
    private String queue;
    private final Map<String, EventProcessor> listeners = new HashMap<>();

    public DirectConsumer init(String exchange, String routinghKey, String queue) {
        this.exchange = exchange;
        this.routinghKey = routinghKey;
        this.queue = queue;
        return this;
    }

    public DirectConsumer addProcessor(String event, EventProcessor listener) {
        listeners.put(event, listener);
        return this;
    }

    /**
     * En caso de desconexión se conectara nuevamente en 10 segundos
     */
    public void startDelayed() {
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                start();
            }
        }, 10 * 1000); // En 10 segundos reintenta.
    }

    /**
     * Conecta a rabbit para escuchar eventos
     */
    public void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(environmentVars.envData.rabbitServerUrl);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchange, "direct");
            channel.queueDeclare(queue, false, false, false, null);

            channel.queueBind(queue, exchange, routinghKey);

            new Thread(() -> {
                try {
                    //logger.info("RabbitMQ Escuchando " + queue);

                    channel.basicConsume(queue, true, new EventConsumer(channel));
                } catch (Exception e) {
                    //logger.error("RabbitMQ ", e);
                    startDelayed();
                }
            }).start();
        } catch (Exception e) {
            //logger.error("RabbitMQ ", e);
            startDelayed();
        }
    }

    class EventConsumer extends DefaultConsumer {

        EventConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
            try {
                // Deserializar el evento RabbitEvent
                System.out.println("Body recibido: "+new String(body));
                RabbitEvent event = RabbitEvent.fromJson(new String(body));
                
                // Validar el evento
                validator.validate(event);

                // Procesar según el tipo de evento
                EventProcessor processor = listeners.get(event.type);

                if (processor != null) {
                    processor.process(event);
                } else {
                    // Procesar otros tipos de eventos si es necesario
                    throw new Error("No existe un precesador para dicho tipo de mensaje");
                }

            } catch (Exception e) {
                System.err.println("Error procesando el evento RabbitMQ: " + e.getMessage());
                e.printStackTrace();
            }
        }

    }
}
