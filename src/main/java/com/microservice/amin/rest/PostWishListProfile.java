/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rest;

import com.microservice.amin.profile.Profile;
import com.microservice.amin.profile.ProfileService;
import com.microservice.amin.rest.dto.WishListProfileRequest;
import com.microservice.amin.security.User;

import com.microservice.amin.wish.WishlistItem;
import com.microservice.amin.wish.WishlistService;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.microservice.amin.security.TokenService;

/**
 *
 * @author cuent
 */
@RestController
public class PostWishListProfile {

    @Autowired
    TokenService tokenService;

    @Autowired
    WishlistService wishlistService;

    @Autowired
    ProfileService profileService;

    @PostMapping("/v1/profile/wishlist")
    public ResponseEntity<String> postWishListProfile(
            @RequestHeader("Authorization") String auth,
            @RequestBody WishListProfileRequest wishListProfileRequest) {

        // Valida que el usuario esté autenticado
        User user = tokenService.validateUser(auth);
        Profile profile = profileService.findProfileByUserID(user.getId());
        if (!profile.getId().equals(wishListProfileRequest.getPofileId())) {
            throw new IllegalArgumentException("Se esta intentando asiganar un producto a otro perfil");
        }

        // Lanza la ejecución del servicio en segundo plano
        CompletableFuture.runAsync(() -> {
            try {
                if (wishListProfileRequest.getAction().equals("add")) {
                    wishlistService.addArticleToWishlist(
                            wishListProfileRequest.getPofileId(),
                            wishListProfileRequest.getArticleId());
                } else if (wishListProfileRequest.getAction().equals("delet")) {
                    wishlistService.deletArticleWishlist(
                            wishListProfileRequest.getPofileId(),
                            wishListProfileRequest.getArticleId());
                } else {
                    throw new Error("La acción solicitada no esta contemplada");
                }

            } catch (Exception e) {
                // Loggea errores que ocurran durante el procesamiento
                System.err.println("Error procesando la wishlist: " + e.getMessage());
            }
        });

        // Responde inmediatamente al cliente
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("El artículo se está validando y fue añadido a la wishlist con estado PENDING_VALIDATION.");
    }

}
