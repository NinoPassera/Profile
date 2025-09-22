/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rabbit.dto;

import com.google.gson.annotations.SerializedName;
import com.microservice.amin.tools.gson.GsonTools;
import com.microservice.amin.tools.rabbit.RabbitEvent;

/**
 *
 * @author cuent
 */
public class SendArticleExist {

    @SerializedName("articleId")
    private String articleId;
    
    @SerializedName("correlationId")
    private String referenceId;
    

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

    

    public static SendArticleExist fromJson(String json) {
        return GsonTools.gson().fromJson(json, SendArticleExist.class);
    }

    @Override
    public String toString() {
        return "SendArticleExist{" +
                "articleId='" + articleId + '\'' +
                ", referenceId='" + referenceId + '\'' +
                '}';
    }
}

