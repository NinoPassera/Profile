package com.microservice.amin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para manejar respuestas HTTP estandarizadas
 */
@Service
public class ResponseService {

    private static final Logger logger = LoggerFactory.getLogger(ResponseService.class);

    /**
     * Crea una respuesta de éxito
     */
    public ResponseEntity<Map<String, Object>> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);

        logger.debug("Respuesta de éxito creada");
        return ResponseEntity.ok(response);
    }

    /**
     * Crea una respuesta de éxito con mensaje personalizado
     */
    public ResponseEntity<Map<String, Object>> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        response.put("data", data);

        logger.debug("Respuesta de éxito creada con mensaje: {}", message);
        return ResponseEntity.ok(response);
    }

    /**
     * Crea una respuesta de error estructurada
     */
    public ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", "error");
        response.put("error", error);
        response.put("code", status.value());

        logger.warn("Error en wishlist - {}: {}", error, message);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Crea una respuesta de error desde una excepción
     */
    public ResponseEntity<Map<String, Object>> createErrorFromException(Exception e) {
        String message = e.getMessage();
        String error = "Error de validación";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // Mapear tipos de error a códigos HTTP apropiados
        if (message.contains("no encontrado") || message.contains("No se encontró")) {
            status = HttpStatus.NOT_FOUND;
            error = "Recurso no encontrado";
        } else if (message.contains("duplicado") || message.contains("ya está")) {
            status = HttpStatus.CONFLICT;
            error = "Conflicto de datos";
        } else if (message.contains("permisos") || message.contains("denegado")) {
            status = HttpStatus.FORBIDDEN;
            error = "Acceso denegado";
        }

        return createErrorResponse(status, error, message);
    }

    /**
     * Crea una respuesta de error interno del servidor
     */
    public ResponseEntity<Map<String, Object>> createInternalServerErrorResponse(String message) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", message);
    }
}
