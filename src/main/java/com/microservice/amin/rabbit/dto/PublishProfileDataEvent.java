/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rabbit.dto;

import com.google.gson.annotations.SerializedName;
import com.microservice.amin.rabbit.PublishProfile;
import jakarta.validation.constraints.NotEmpty;
import java.util.Date;

/**
 *
 * @author cuent
 */
public final class PublishProfileDataEvent {
 
    @NotEmpty(message= "No puede estar vacio")
    @SerializedName("profileId")
    public  String profileId;
    
    @NotEmpty(message= "No puede estar vacio")
    @SerializedName("userId")
    public  String userId;
    
    @NotEmpty(message= "No puede estar vacio")
    @SerializedName("firstName")
    public  String firstName;
    
    
    @NotEmpty(message= "No puede estar vacio")
    @SerializedName("lastName")
    public  String lastName;
    
    @NotEmpty(message= "No puede estar vacio")
    @SerializedName("imageId")
    public  String imageId;
    
    @NotEmpty(message= "No puede estar vacio")
    @SerializedName("creationDate")
    public final Date creationDate;
    
    @NotEmpty(message= "No puede estar vacio")
    @SerializedName("updateDate")
    public final Date updateDate;

    public PublishProfileDataEvent(String profileId, String userId, String firstName, String lastName, String imageId, Date creationDate, Date updateDate) {
        this.profileId = profileId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageId = imageId;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    public PublishProfileDataEvent(Date creationDate, Date updateDate) {
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }
    
    

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
    
    

    
 
}
