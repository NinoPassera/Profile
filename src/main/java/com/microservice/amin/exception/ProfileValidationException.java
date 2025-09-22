package com.microservice.amin.exception;

/**
 * Excepción personalizada para errores de validación de perfil
 * 
 * @author cuent
 */
public class ProfileValidationException extends RuntimeException {

    public ProfileValidationException(String message) {
        super(message);
    }

    public ProfileValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
