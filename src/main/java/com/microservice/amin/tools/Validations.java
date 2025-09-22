package com.microservice.amin.tools;

import com.microservice.amin.security.TokenService;
import com.microservice.amin.security.User;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author cuent
 */
@Service
public class Validations {

    public boolean validarImagen(String imageId, String token) {
        // Validar que imageId no sea null o vacío
        if (imageId == null || imageId.trim().isEmpty()) {
            System.out.println("ImageId es null o vacío");
            return false;
        }

        // Crear el cliente HTTP
        HttpClient client = HttpClientBuilder.create().build();

        // Construir la URL para el endpoint del microservicio según la especificación
        String url = String.format("http://localhost:3001/images/%s", imageId);
        HttpGet request = new HttpGet(url);

        // Agregar headers según la especificación de ImageGo
        request.addHeader("Authorization", "Bearer " + token);
        request.addHeader("correlation_id", java.util.UUID.randomUUID().toString());

        try {
            // Ejecutar la solicitud
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            System.out.println("Validación de imagen - Status Code: " + statusCode);

            // Según la especificación: 200 = imagen existe, otros códigos = error
            if (statusCode == 200) {
                System.out.println("Imagen válida encontrada: " + imageId);
                return true;
            } else if (statusCode == 404) {
                System.out.println("Imagen no encontrada: " + imageId);
                return false;
            } else if (statusCode == 401) {
                System.out.println("Token de autorización inválido para imagen: " + imageId);
                return false;
            } else {
                System.out.println("Error del servidor al validar imagen: " + imageId + ", Status: " + statusCode);
                return false;
            }

        } catch (Exception e) {
            System.out.println("Error de conexión al validar imagen: " + imageId);
            e.printStackTrace();
            return false;
        }
    }

}
