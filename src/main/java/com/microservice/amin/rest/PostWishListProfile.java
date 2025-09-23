/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rest;

import com.microservice.amin.rest.dto.WishListProfileRequest;
import com.microservice.amin.security.User;
import com.microservice.amin.service.ResponseService;
import com.microservice.amin.wish.WishlistService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private static final Logger logger = LoggerFactory.getLogger(PostWishListProfile.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private ResponseService responseService;

    @PostMapping("/v1/profile/wishlist")
    public ResponseEntity<Map<String, Object>> postWishListProfile(
            @RequestHeader("Authorization") String auth,
            @RequestBody WishListProfileRequest wishListProfileRequest) {

        logger.info("Recibida solicitud para gestionar wishlist");

        try {
            // Valida que el usuario esté autenticado
            User user = tokenService.validateUser(auth);
            logger.debug("Usuario validado exitosamente: {}", user.getId());

            // Validar la solicitud usando el servicio
            wishlistService.validateWishlistRequest(
                    wishListProfileRequest.getProfileId(),
                    wishListProfileRequest.getAction(),
                    wishListProfileRequest.getArticleId());

            // Validar permisos del usuario
            wishlistService.validateUserPermissions(user.getId(), wishListProfileRequest.getProfileId());

            // Validar acción específica
            if (wishListProfileRequest.getAction().equals("add")) {
                wishlistService.validateArticleNotInWishlist(
                        wishListProfileRequest.getProfileId(),
                        wishListProfileRequest.getArticleId());
            } else if (wishListProfileRequest.getAction().equals("delete")) {
                wishlistService.validateArticleExistsInWishlist(
                        wishListProfileRequest.getProfileId(),
                        wishListProfileRequest.getArticleId());
            }

            // Ejecutar la acción en segundo plano
            executeWishlistActionAsync(wishListProfileRequest);

            // Crear respuesta exitosa
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("action", wishListProfileRequest.getAction());
            responseData.put("articleId", wishListProfileRequest.getArticleId());
            responseData.put("profileId", wishListProfileRequest.getProfileId());

            String successMessage = wishlistService.getSuccessMessage(wishListProfileRequest.getAction());

            logger.info("Solicitud de wishlist procesada exitosamente. Acción: {}, ArticleId: {}",
                    wishListProfileRequest.getAction(), wishListProfileRequest.getArticleId());

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(responseService.createSuccessResponse(successMessage, responseData).getBody());

        } catch (IllegalArgumentException e) {
            return responseService.createErrorFromException(e);
        } catch (Exception e) {
            logger.error("Error inesperado en postWishListProfile: {}", e.getMessage(), e);
            return responseService.createInternalServerErrorResponse(
                    "Ocurrió un error inesperado al procesar la solicitud");
        }
    }

    /**
     * Ejecuta la acción de wishlist en segundo plano
     */
    private void executeWishlistActionAsync(WishListProfileRequest request) {
        CompletableFuture.runAsync(() -> {
            try {
                if (request.getAction().equals("add")) {
                    wishlistService.addArticleToWishlist(
                            request.getProfileId(),
                            request.getArticleId());
                } else if (request.getAction().equals("delete")) {
                    wishlistService.deleteArticleWishlist(
                            request.getProfileId(),
                            request.getArticleId());
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Error de validación en wishlist: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("Error inesperado procesando la wishlist: {}", e.getMessage(), e);
            }
        });
    }

}
