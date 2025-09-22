/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rest.dto;

import java.util.List;

/**
 *
 * @author cuent
 */
public class ProfileResponseDTO {

    private String id;
    private String userId;
    private String name;
    private String email;
    private List<String> wishList;
    private String imageId;
    private long creationDate;
    private long updateDate;

    // Constructor
    public ProfileResponseDTO(String id, String userId, String name, String email, 
                              List<String> wishList, String imageId, long creationDate, long updateDate) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.wishList = wishList;
        this.imageId = imageId;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    public ProfileResponseDTO() {
    }
    
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getWishList() {
        return wishList;
    }

    public void setWishList(List<String> wishList) {
        this.wishList = wishList;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    
  
}
