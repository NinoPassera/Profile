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
 * Consumidor para exchanges de tipo fanout.
 * Recibe todos los mensajes enviados al exchange al que está enlazado.
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FanoutConsumer {
    @Autowired
    EnvironmentVars environmentVars;

    @Autowired
    ValidatorService validator;

    private String exchange;
    private String queue;
    private final Map<String, EventProcessor> listeners = new HashMap<>();

    public FanoutConsumer init(String exchange, String queue) {
        this.exchange = exchange;
        this.queue = queue;
        return this;
    }

    public FanoutConsumer addProcessor(String event, EventProcessor listener) {
        listeners.put(event, listener);
        return this;
    }

    public void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(environmentVars.envData.rabbitServerUrl);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // Declara el exchange como fanout
            channel.exchangeDeclare(exchange, "fanout");
            // Declara una cola temporal si no se proporciona una
            if (queue == null || queue.isEmpty()) {
                queue = channel.queueDeclare().getQueue();
            }
            // Enlaza la cola al exchange
            channel.queueBind(queue, exchange, "");

            new Thread(() -> {
                try {
                    System.out.println("RabbitMQ escuchando en el fanout exchange: " + exchange);

                    channel.basicConsume(queue, true, new FanoutEventConsumer(channel));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class FanoutEventConsumer extends DefaultConsumer {
        FanoutEventConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
            try {
                RabbitEvent event = RabbitEvent.fromJson(new String(body));
                validator.validate(event);

                EventProcessor processor = listeners.get(event.type);
                if (processor != null) {
                    System.out.println("Procesando evento del tipo: " + event.type);
                    processor.process(event);
                } else {
                    System.out.println("No se encontró un processor para el tipo: " + event.type);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
