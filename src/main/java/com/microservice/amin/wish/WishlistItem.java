/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.wish;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author cuent
 */
@Document
public class WishlistItem {

    @Id
    private String id;


    private String profileId;
    private String articleId; // ID del artícul
    private WishlistStatus status; // Enum para el estado
    private Date createdAt; // Fecha de creación
    private String correlationId;
    
    // Constructor, getters y setters
    public WishlistItem(String profileId, String articleId,String correlationId) {
        this.profileId = profileId;
        this.articleId = articleId;
        this.status = WishlistStatus.PENDING_VALIDATION; // Estado inicial
        this.createdAt = new Date();
        this.correlationId = correlationId;
    }

    public WishlistItem() {
        // Constructor vacío para JPA
    }

    public WishlistStatus getStatus() {
        return status;
    }

    public void setStatus(WishlistStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
}
