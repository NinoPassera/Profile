package com.microservice.amin.rabbit.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para la solicitud de validación de artículo al catálogo
 */
public class ArticleExistRequest {

    @SerializedName("correlation_id")
    private String correlationId;

    @SerializedName("routing_key")
    private String routingKey;

    @SerializedName("exchange")
    private String exchange;

    @SerializedName("message")
    private ArticleExistMessage message;

    // Constructor
    public ArticleExistRequest(String correlationId, String routingKey, String exchange, ArticleExistMessage message) {
        this.correlationId = correlationId;
        this.routingKey = routingKey;
        this.exchange = exchange;
        this.message = message;
    }

    // Getters y setters
    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public ArticleExistMessage getMessage() {
        return message;
    }

    public void setMessage(ArticleExistMessage message) {
        this.message = message;
    }

    public static class ArticleExistMessage {
        @SerializedName("referenceId")
        private String referenceId;

        @SerializedName("articleId")
        private String articleId;

        // Constructor
        public ArticleExistMessage(String referenceId, String articleId) {
            this.referenceId = referenceId;
            this.articleId = articleId;
        }

        // Getters y setters
        public String getReferenceId() {
            return referenceId;
        }

        public void setReferenceId(String referenceId) {
            this.referenceId = referenceId;
        }

        public String getArticleId() {
            return articleId;
        }

        public void setArticleId(String articleId) {
            this.articleId = articleId;
        }
    }
}
