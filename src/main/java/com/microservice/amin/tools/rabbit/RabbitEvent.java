package com.microservice.amin.tools.rabbit;


import com.google.gson.annotations.SerializedName;
import com.microservice.amin.tools.gson.GsonTools;


public class RabbitEvent {
    // tipo de mensaje enviado
    @SerializedName("type")
    public String type;

    // Version del protocolo
    @SerializedName("version")
    public int version;

    // Por si el destinatario necesita saber de donde viene el mensaje
    @SerializedName("queue")
    public String queue;
    
    // Por si el destinatario necesita saber donde responder 
    @SerializedName("routingKey")
    public String routingKey;
    
    // Por si el destinatario necesita saber de donde viene el mensaje
    @SerializedName("exchange")
    public String exchange;

    // El body del mensaje
    @SerializedName("message")
    public Object message;

    public static RabbitEvent fromJson(String json) {
        return GsonTools.gson().fromJson(json, RabbitEvent.class);
    }

    @Override
    public String toString() {
        return "RabbitEvent{" + "type=" + type + ", version=" + version + ", queue=" + queue + ", routingKey=" + routingKey + ", exchange=" + exchange + ", message=" + message + '}';
    }
    
}