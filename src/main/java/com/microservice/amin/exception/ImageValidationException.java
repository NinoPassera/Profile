package com.microservice.amin.exception;

/**
 * Excepción personalizada para errores de validación de imagen
 * 
 * @author cuent
 */
public class ImageValidationException extends RuntimeException {

    public ImageValidationException(String message) {
        super(message);
    }

    public ImageValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
