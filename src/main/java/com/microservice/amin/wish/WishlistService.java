/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.wish;

import com.microservice.amin.profile.Profile;
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
        if (profileService.getProfile(profileId) == null) {
            throw new IllegalArgumentException("No existe un perfil al que asignar el artículo.");
        }

        // Verifica si el artículo ya está en la wishlist
        Optional<WishlistItem> existingItem = wishlistRepository.findByProfileIdAndArticleId(profileId, articleId);
        if (existingItem.isPresent()) {
            throw new IllegalArgumentException("El artículo ya está en la wishlist.");
        }
    }

    public void validateArticleExistsInWishlist(String profileId, String articleId) {
        // Validaciones de entrada
        if (profileId == null || profileId.trim().isEmpty()) {
            throw new IllegalArgumentException("El profileId no puede ser null o vacío.");
        }

        if (articleId == null || articleId.trim().isEmpty()) {
            throw new IllegalArgumentException("El articleId no puede ser null o vacío.");
        }

        // Verifica que exista un perfil
        if (profileService.getProfile(profileId) == null) {
            throw new IllegalArgumentException("No existe un perfil al que asignar el artículo.");
        }

        // Verifica si el artículo está en la wishlist
        Optional<WishlistItem> existingItem = wishlistRepository.findByProfileIdAndArticleId(profileId, articleId);
        if (!existingItem.isPresent()) {
            throw new IllegalArgumentException("El artículo no está en la wishlist.");
        }
    }

    /**
     * Valida que el usuario tenga permisos para modificar el perfil
     */
    public void validateUserPermissions(String userId, String profileId) {
        Profile targetProfile = profileService.getProfile(profileId);
        if (targetProfile == null) {
            throw new IllegalArgumentException("No se encontró el perfil especificado");
        }

        if (!targetProfile.getUserId().equals(userId)) {
            throw new IllegalArgumentException("No tienes permisos para modificar este perfil");
        }
    }

    /**
     * Valida la solicitud de wishlist
     */
    public void validateWishlistRequest(String profileId, String action, String articleId) {
        if (profileId == null || profileId.trim().isEmpty()) {
            throw new IllegalArgumentException("El profileId es requerido");
        }

        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("La acción es requerida");
        }

        if (!action.equals("add") && !action.equals("delete")) {
            throw new IllegalArgumentException("La acción debe ser 'add' o 'delete'");
        }

        if (articleId == null || articleId.trim().isEmpty()) {
            throw new IllegalArgumentException("El articleId es requerido");
        }
    }

    /**
     * Obtiene el mensaje de éxito según la acción
     */
    public String getSuccessMessage(String action) {
        if ("add".equals(action)) {
            return "El artículo se está validando y será añadido a la wishlist con estado PENDING_VALIDATION.";
        } else if ("delete".equals(action)) {
            return "El artículo está siendo eliminado de la wishlist.";
        }
        return "Operación de wishlist procesada exitosamente.";
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
        if (profileService.getProfile(profileId) == null) {
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
        if (profileService.getProfile(profileId) == null) {
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

    /**
     * Elimina artículos comprados de la lista de deseos del usuario
     * Este método se ejecuta cuando se recibe un evento order_placed
     * 
     * @param userId              ID del usuario que realizó la compra
     * @param purchasedArticleIds Lista de IDs de artículos comprados
     * @return Número de artículos eliminados de la wishlist
     */
    public int removePurchasedArticlesFromWishlist(String userId, java.util.List<String> purchasedArticleIds) {
        // Validaciones de entrada
        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("UserId nulo o vacío recibido para eliminación de artículos comprados");
            return 0;
        }

        if (purchasedArticleIds == null || purchasedArticleIds.isEmpty()) {
            logger.warn("Lista de artículos comprados vacía para userId: {}", userId);
            return 0;
        }

        logger.info("Procesando eliminación de artículos comprados para userId: {}, artículos: {}",
                userId, purchasedArticleIds);

        try {
            // Verifica que exista un perfil y obtiene el profileId
            Profile userProfile = profileService.findProfileByUserID(userId);
            if (userProfile == null) {
                logger.warn("No existe un perfil para userId: {}, ignorando evento order_placed", userId);
                return 0;
            }

            String profileId = userProfile.getId();
            logger.debug("Perfil encontrado - ProfileId: {}, UserId: {}", profileId, userId);

            int removedCount = 0;

            // Procesa cada artículo comprado
            for (String articleId : purchasedArticleIds) {
                if (articleId == null || articleId.trim().isEmpty()) {
                    logger.warn("ArticleId nulo o vacío, saltando eliminación");
                    continue;
                }

                // Busca el artículo en la wishlist del usuario usando profileId
                java.util.Optional<WishlistItem> wishlistItem = wishlistRepository.findByProfileIdAndArticleId(
                        profileId,
                        articleId);

                if (wishlistItem.isPresent()) {
                    try {
                        // Elimina el artículo de la wishlist
                        wishlistRepository.delete(wishlistItem.get());
                        removedCount++;
                        logger.info("Artículo {} eliminado de wishlist del usuario {}", articleId, userId);
                    } catch (Exception e) {
                        logger.error("Error al eliminar artículo {} de wishlist del usuario {}: {}",
                                articleId, userId, e.getMessage());
                    }
                } else {
                    logger.debug("Artículo {} no encontrado en wishlist del usuario {}", articleId, userId);
                }
            }

            logger.info("Eliminación completada para userId: {}, {} artículos removidos de wishlist",
                    userId, removedCount);

            return removedCount;

        } catch (Exception e) {
            logger.error("Error procesando eliminación de artículos comprados para userId {}: {}",
                    userId, e.getMessage(), e);
            throw new RuntimeException("Error al procesar eliminación de artículos de wishlist", e);
        }
    }
}
