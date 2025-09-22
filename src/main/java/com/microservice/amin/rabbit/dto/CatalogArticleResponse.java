package com.microservice.amin.rabbit.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para la respuesta del catálogo sobre validación de artículos
 */
public class CatalogArticleResponse {

    @SerializedName("correlation_id")
    private String correlationId;

    @SerializedName("message")
    private ArticleExistMessage message;

    // Getters y setters
    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public ArticleExistMessage getMessage() {
        return message;
    }

    public void setMessage(ArticleExistMessage message) {
        this.message = message;
    }

    public static class ArticleExistMessage {
        @SerializedName("articleId")
        private String articleId;

        @SerializedName("referenceId")
        private String referenceId;

        @SerializedName("price")
        private float price;

        @SerializedName("stock")
        private int stock;

        @SerializedName("valid")
        private boolean valid;

        // Getters y setters
        public String getArticleId() {
            return articleId;
        }

        public void setArticleId(String articleId) {
            this.articleId = articleId;
        }

        public String getReferenceId() {
            return referenceId;
        }

        public void setReferenceId(String referenceId) {
            this.referenceId = referenceId;
        }

        public float getPrice() {
            return price;
        }

        public void setPrice(float price) {
            this.price = price;
        }

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }
    }
}
