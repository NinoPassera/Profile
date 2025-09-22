/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rest.dto;

/**
 *
 * @author cuent
 */
public class WishListProfileRequest {
    private String pofileId;
    private String articleId;
    private String action;

    public String getPofileId() {
        return pofileId;
    }

    public void setPofileId(String pofileId) {
        this.pofileId = pofileId;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    
    
    
}
