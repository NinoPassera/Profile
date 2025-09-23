/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rest;

import com.microservice.amin.profile.Profile;
import com.microservice.amin.profile.ProfileService;
import com.microservice.amin.rest.dto.WishListProfileRequest;
import com.microservice.amin.security.User;
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
    TokenService tokenService;

    @Autowired
    WishlistService wishlistService;

    @Autowired
    ProfileService profileService;

    @PostMapping("/v1/profile/wishlist")
    public ResponseEntity<Map<String, Object>> postWishListProfile(
            @RequestHeader("Authorization") String auth,
            @RequestBody WishListProfileRequest wishListProfileRequest) {

        logger.info("Recibida solicitud para gestionar wishlist");

        try {
            // Valida que el usuario esté autenticado
            User user = tokenService.validateUser(auth);
            logger.debug("Usuario validado exitosamente: {}", user.getId());

            // Validaciones de entrada primero
            if (wishListProfileRequest.getProfileId() == null
                    || wishListProfileRequest.getProfileId().trim().isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "ProfileId requerido",
                        "El profileId es requerido");
            }

            // Buscar el perfil que se está intentando modificar
            Profile targetProfile = profileService.getProfile(wishListProfileRequest.getProfileId());
            if (targetProfile == null) {
                return createErrorResponse(HttpStatus.NOT_FOUND, "Perfil no encontrado",
                        "No se encontró el perfil especificado");
            }

            // Verificar que el perfil pertenece al usuario autenticado
            if (!targetProfile.getUserId().equals(user.getId())) {
                return createErrorResponse(HttpStatus.FORBIDDEN, "Acceso denegado",
                        "No tienes permisos para modificar este perfil");
            }

            if (wishListProfileRequest.getAction() == null || wishListProfileRequest.getAction().trim().isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Acción requerida",
                        "La acción es requerida");
            }

            if (!wishListProfileRequest.getAction().equals("add")
                    && !wishListProfileRequest.getAction().equals("delete")) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Acción inválida",
                        "La acción debe ser 'add' o 'delete'");
            }

            if (wishListProfileRequest.getArticleId() == null
                    || wishListProfileRequest.getArticleId().trim().isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "ArticleId requerido",
                        "El articleId es requerido");
            }

            // Validar si el artículo ya existe en la wishlist (para acción "add")
            if (wishListProfileRequest.getAction().equals("add")) {
                try {
                    wishlistService.validateArticleNotInWishlist(
                            wishListProfileRequest.getProfileId(),
                            wishListProfileRequest.getArticleId());
                } catch (IllegalArgumentException e) {
                    return createErrorResponse(HttpStatus.CONFLICT, "Artículo duplicado",
                            e.getMessage());
                }
            }

            // Validar si el artículo existe en la wishlist (para acción "delete")
            if (wishListProfileRequest.getAction().equals("delete")) {
                try {
                    wishlistService.validateArticleExistsInWishlist(
                            wishListProfileRequest.getProfileId(),
                            wishListProfileRequest.getArticleId());
                } catch (IllegalArgumentException e) {
                    return createErrorResponse(HttpStatus.NOT_FOUND, "Artículo no encontrado",
                            e.getMessage());
                }
            }

            // Lanza la ejecución del servicio en segundo plano
            CompletableFuture.runAsync(() -> {
                try {
                    if (wishListProfileRequest.getAction().equals("add")) {
                        wishlistService.addArticleToWishlist(
                                wishListProfileRequest.getProfileId(),
                                wishListProfileRequest.getArticleId());
                    } else if (wishListProfileRequest.getAction().equals("delete")) {
                        wishlistService.deleteArticleWishlist(
                                wishListProfileRequest.getProfileId(),
                                wishListProfileRequest.getArticleId());
                    }

                } catch (IllegalArgumentException e) {
                    // Errores de validación - se loggean pero no se propagan
                    logger.warn("Error de validación en wishlist: {}", e.getMessage());
                } catch (Exception e) {
                    // Otros errores - se loggean para debugging
                    logger.error("Error inesperado procesando la wishlist: {}", e.getMessage(), e);
                }
            });

            // Crear respuesta exitosa
            Map<String, Object> response = new HashMap<>();
            response.put("message", getSuccessMessage(wishListProfileRequest.getAction()));
            response.put("status", "success");
            response.put("action", wishListProfileRequest.getAction());
            response.put("articleId", wishListProfileRequest.getArticleId());
            response.put("profileId", wishListProfileRequest.getProfileId());

            logger.info("Solicitud de wishlist procesada exitosamente. Acción: {}, ArticleId: {}",
                    wishListProfileRequest.getAction(), wishListProfileRequest.getArticleId());

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (Exception e) {
            logger.error("Error inesperado en postWishListProfile: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor",
                    "Ocurrió un error inesperado al procesar la solicitud");
        }
    }

    /**
     * Crea una respuesta de error estructurada
     * 
     * @param status  Código de estado HTTP
     * @param error   Tipo de error
     * @param message Mensaje descriptivo del error
     * @return ResponseEntity con la respuesta de error
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", "error");
        response.put("error", error);
        response.put("code", status.value());

        logger.warn("Error en wishlist - {}: {}", error, message);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Obtiene el mensaje de éxito según la acción realizada
     * 
     * @param action Acción realizada ("add" o "delete")
     * @return Mensaje de éxito apropiado
     */
    private String getSuccessMessage(String action) {
        if ("add".equals(action)) {
            return "El artículo se está validando y será añadido a la wishlist con estado PENDING_VALIDATION.";
        } else if ("delete".equals(action)) {
            return "El artículo está siendo eliminado de la wishlist.";
        }
        return "Operación de wishlist procesada exitosamente.";
    }

}
