package com.microservice.amin.tools.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utilidad para serialización/deserialización JSON usando Gson
 * Proporciona métodos estáticos para conversión de objetos
 */
public class GsonTools {

    private static final Gson gson = new GsonBuilder().create();

    /**
     * Obtiene una instancia de Gson configurada
     */
    public static Gson gson() {
        return gson;
    }

    /**
     * Convierte un objeto a JSON
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * Convierte JSON a un objeto del tipo especificado
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}
