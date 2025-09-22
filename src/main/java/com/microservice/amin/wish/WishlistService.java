/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.wish;

import com.microservice.amin.profile.ProfileService;
import com.microservice.amin.rabbit.PublisherArticleExist;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author cuent
 */
@Service
public class WishlistService {

    private static final Logger logger = LoggerFactory.getLogger(WishlistService.class);

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private PublisherArticleExist publisherArticleExist;

    // @Autowired
    // private ArticleExistResponseConsumer articleExistResponseConsumer;
    //
    // @Async
    // public void validateArticleAsync(String profileId, String articleId, String
    // correlationId) {
    // try {
    //
    // // Publica el mensaje a RabbitMQ
    // publisherArticleExist.publishArticleExist(articleId, correlationId);
    //
    // // Espera la respuesta asociada al correlationId
    // Boolean isValid =
    // articleExistResponseConsumer.waitForResponse(correlationId);
    //
    // // Si no llega respuesta, marcar como inválido
    // if (isValid == null) {
    // isValid = false;
    // }
    //
    // // Actualiza el estado del artículo en la base de datos
    // //updateArticleStatus(profileId, articleId, isValid);
    // } catch (Exception e) {
    // // Si hay un error, marca el artículo como INVALID
    // //updateArticleStatus(profileId, articleId, false);
    // System.err.println("Error durante la validación: " + e.getMessage());
    // }
    // }

    /**
     * Valida que un artículo no esté ya en la wishlist del perfil
     * 
     * @param profileId ID del perfil
     * @param articleId ID del artículo
     * @throws IllegalArgumentException si el artículo ya está en la wishlist
     */
    public void validateArticleNotInWishlist(String profileId, String articleId) {
        // Validaciones de entrada
        if (profileId == null || profileId.trim().isEmpty()) {
            throw new IllegalArgumentException("El profileId no puede ser null o vacío.");
        }

        if (articleId == null || articleId.trim().isEmpty()) {
            throw new IllegalArgumentException("El articleId no puede ser null o vacío.");
        }

        // Verifica que exista un perfil
        if (!profileService.existProfileByID(profileId)) {
            throw new IllegalArgumentException("No existe un perfil al que asignar el artículo.");
        }

        // Verifica si el artículo ya está en la wishlist
        Optional<WishlistItem> existingItem = wishlistRepository.findByProfileIdAndArticleId(profileId, articleId);
        if (existingItem.isPresent()) {
            throw new IllegalArgumentException("El artículo ya está en la wishlist.");
        }
    }

    public void addArticleToWishlist(String profileId, String articleId) {
        // Validaciones de entrada
        if (profileId == null || profileId.trim().isEmpty()) {
            throw new IllegalArgumentException("El profileId no puede ser null o vacío.");
        }

        if (articleId == null || articleId.trim().isEmpty()) {
            throw new IllegalArgumentException("El articleId no puede ser null o vacío.");
        }

        // Verifica que exista un perfil
        if (!profileService.existProfileByID(profileId)) {
            throw new IllegalArgumentException("No existe un perfil al que asignar el artículo.");
        }

        String correlationId = java.util.UUID.randomUUID().toString();
        logger.info("Agregando artículo {} a wishlist del perfil {} con correlationId: {}",
                articleId, profileId, correlationId);

        try {
            // PRIMERO: Crear y guardar el artículo con estado PENDING_VALIDATION
            WishlistItem newItem = new WishlistItem(profileId, articleId, correlationId);
            wishlistRepository.save(newItem);

            logger.info("Artículo {} guardado en wishlist con estado PENDING_VALIDATION", articleId);

            // SEGUNDO: Publicar mensaje al catálogo para validación
            publisherArticleExist.publishArticleExist(articleId, correlationId);

            logger.info("Artículo {} agregado exitosamente a wishlist del perfil {}", articleId, profileId);
        } catch (Exception e) {
            logger.error("Error al agregar artículo {} a wishlist del perfil {}: {}",
                    articleId, profileId, e.getMessage());
            throw new RuntimeException("Error al procesar la solicitud de wishlist", e);
        }
    }

    public void deleteArticleWishlist(String profileId, String articleId) {
        // Validaciones de entrada
        if (profileId == null || profileId.trim().isEmpty()) {
            throw new IllegalArgumentException("El profileId no puede ser null o vacío.");
        }

        if (articleId == null || articleId.trim().isEmpty()) {
            throw new IllegalArgumentException("El articleId no puede ser null o vacío.");
        }

        // Verifica que exista un perfil
        if (!profileService.existProfileByID(profileId)) {
            throw new IllegalArgumentException("No existe un perfil al que asignar el artículo.");
        }

        logger.info("Eliminando artículo {} de wishlist del perfil {}", articleId, profileId);

        Optional<WishlistItem> existingItem = wishlistRepository.findByProfileIdAndArticleId(profileId, articleId);

        if (!existingItem.isPresent()) {
            throw new IllegalArgumentException("El artículo que quieres eliminar no está en la wishlist");
        }

        try {
            WishlistItem wishlistItem = existingItem.get();
            wishlistRepository.delete(wishlistItem);
            logger.info("Artículo {} eliminado exitosamente de wishlist del perfil {}", articleId, profileId);
        } catch (Exception e) {
            logger.error("Error al eliminar artículo {} de wishlist del perfil {}: {}",
                    articleId, profileId, e.getMessage());
            throw new RuntimeException("Error al eliminar artículo de wishlist", e);
        }
    }

    public void updateArticleStatus(String correlationId, boolean isValid) {
        if (correlationId == null || correlationId.trim().isEmpty()) {
            logger.warn("CorrelationId nulo o vacío recibido para actualización de estado");
            return;
        }

        try {
            WishlistItem item = wishlistRepository.findByCorrelationId(correlationId)
                    .orElseThrow(() -> new IllegalArgumentException("El artículo no existe en la wishlist."));

            WishlistStatus newStatus = isValid ? WishlistStatus.VALID : WishlistStatus.INVALID;
            item.setStatus(newStatus);

            wishlistRepository.save(item);

            logger.info("Estado del artículo {} actualizado a {} para correlationId: {}",
                    item.getArticleId(), newStatus, correlationId);
        } catch (Exception e) {
            logger.error("Error al actualizar estado del artículo con correlationId {}: {}",
                    correlationId, e.getMessage());
            throw new RuntimeException("Error al actualizar estado del artículo", e);
        }
    }

    public List<WishlistItem> findWishListItemByProfileId(String profileId) {
        return wishlistRepository.findByProfileIdAndStatus(profileId, WishlistStatus.VALID);
    }
}
