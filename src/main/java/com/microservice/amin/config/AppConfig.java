package com.microservice.amin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración centralizada de la aplicación
 * Maneja todas las variables de entorno y valores por defecto
 */
@ConfigurationProperties(prefix = "app")
@Component
public class AppConfig {

    // RabbitMQ Configuration
    private String rabbitHost = "localhost";
    private String rabbitExchange = "profile_exchange";

    // Database Configuration
    private String mongoUri = "mongodb://localhost:27017/";
    private String mongoDatabase = "profile";

    // External Services
    private String authServiceUrl = "http://localhost:3000";
    private String catalogServiceUrl = "http://localhost:3002";
    private String fluentUrl = "localhost:24224";

    // Server Configuration
    private int port = 8080;

    // Getters and Setters
    public String getRabbitHost() {
        return rabbitHost;
    }

    public void setRabbitHost(String rabbitHost) {
        this.rabbitHost = rabbitHost;
    }

    public String getRabbitExchange() {
        return rabbitExchange;
    }

    public void setRabbitExchange(String rabbitExchange) {
        this.rabbitExchange = rabbitExchange;
    }

    public String getMongoUri() {
        return mongoUri;
    }

    public void setMongoUri(String mongoUri) {
        this.mongoUri = mongoUri;
    }

    public String getMongoDatabase() {
        return mongoDatabase;
    }

    public void setMongoDatabase(String mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    public String getAuthServiceUrl() {
        return authServiceUrl;
    }

    public void setAuthServiceUrl(String authServiceUrl) {
        this.authServiceUrl = authServiceUrl;
    }

    public String getCatalogServiceUrl() {
        return catalogServiceUrl;
    }

    public void setCatalogServiceUrl(String catalogServiceUrl) {
        this.catalogServiceUrl = catalogServiceUrl;
    }

    public String getFluentUrl() {
        return fluentUrl;
    }

    public void setFluentUrl(String fluentUrl) {
        this.fluentUrl = fluentUrl;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
