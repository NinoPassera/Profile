/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.security;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author cuent
 */
@Service
public class TokenService {

    public User validate(String token) throws Error {
        if (token.isEmpty()) {

            throw new Error("Unauthorized");

        }
        User user = retrieveUser(token);
        if (user == null) {

            throw new Error("Unauthorized");
        }

        return user;
    }

    /**
     * Valida un usuario desde el header de autorización
     * 
     * @param authHeader Header de autorización (Bearer token)
     * @return Usuario validado
     * @throws IllegalArgumentException si el token es inválido
     */
    public User validateUser(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Token de autorización requerido");
        }

        User user = retrieveUser(authHeader);
        if (user == null) {
            throw new IllegalArgumentException("Token de autorización inválido");
        }

        return user;
    }

    private User retrieveUser(String token) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("http://localhost:3000/users/current");
        request.addHeader("Authorization", token);
        HttpResponse response;
        try {

            response = client.execute(request);

            // if (response.getStatusLine().getStatusCode() != 200) {
            // return null;
            // }

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity == null) {
                return null;
            }
            String body = EntityUtils.toString(responseEntity);

            return User.fromJson(body);
        } catch (Exception e) {
            return null;
        }
    }
}
