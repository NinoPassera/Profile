
package com.microservice.amin.rabbit.dto;

public class WishItemDataEvent {
    private String articleId;
    private String correlationId;

    // Constructor
    public WishItemDataEvent(String articleId, String correlationId) {
        this.articleId = articleId;
       
        this.correlationId = correlationId;
    }

    // Getters y Setters
    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

   
  

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}

