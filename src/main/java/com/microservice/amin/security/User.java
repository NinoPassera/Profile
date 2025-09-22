package com.microservice.amin.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 *
 * Clase de usuario para manejar datos serializados de JSON
 */
public class User {
    @SerializedName("id")
    public String id;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("login")
    public String login;
    
    @SerializedName("permissions")
    public String[] permissions;

    // Método para deserializar JSON a objeto User
    public static User fromJson(String json) {
        Gson gson = new GsonBuilder().create(); // Construimos un objeto Gson
        return gson.fromJson(json, User.class);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }
    
    
}

