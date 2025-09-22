package com.microservice.amin.rabbit.dto;

import java.util.List;

/**
 * DTO para el evento order_placed que envía el microservicio Orders
 * 
 * Estructura del mensaje:
 * {
 * "orderId": "string",
 * "cartId": "string",
 * "userId": "string",
 * "articles": [
 * {
 * "articleId": "string",
 * "quantity": "integer"
 * }
 * ]
 * }
 */
public class OrderPlacedEvent {

    private String orderId;
    private String cartId;
    private String userId;
    private List<ArticleOrder> articles;

    // Constructores
    public OrderPlacedEvent() {
    }

    public OrderPlacedEvent(String orderId, String cartId, String userId, List<ArticleOrder> articles) {
        this.orderId = orderId;
        this.cartId = cartId;
        this.userId = userId;
        this.articles = articles;
    }

    // Getters y Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<ArticleOrder> getArticles() {
        return articles;
    }

    public void setArticles(List<ArticleOrder> articles) {
        this.articles = articles;
    }

    /**
     * Clase interna para representar los artículos en la orden
     */
    public static class ArticleOrder {
        private String articleId;
        private Integer quantity;

        // Constructores
        public ArticleOrder() {
        }

        public ArticleOrder(String articleId, Integer quantity) {
            this.articleId = articleId;
            this.quantity = quantity;
        }

        // Getters y Setters
        public String getArticleId() {
            return articleId;
        }

        public void setArticleId(String articleId) {
            this.articleId = articleId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return "ArticleOrder{" +
                    "articleId='" + articleId + '\'' +
                    ", quantity=" + quantity +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "OrderPlacedEvent{" +
                "orderId='" + orderId + '\'' +
                ", cartId='" + cartId + '\'' +
                ", userId='" + userId + '\'' +
                ", articles=" + articles +
                '}';
    }
}
