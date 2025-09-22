/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.wish;

import com.microservice.amin.profile.ProfileService;
import com.microservice.amin.rabbit.ArticleExistResponseConsumer;
import com.microservice.amin.rabbit.PublisherArticleExist;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * @author cuent
 */
@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private PublisherArticleExist publisherArticleExist; 
    
//    @Autowired
//    private ArticleExistResponseConsumer articleExistResponseConsumer;
//
//    @Async
//    public void validateArticleAsync(String profileId, String articleId, String correlationId) {
//        try {
//            
//            // Publica el mensaje a RabbitMQ
//            publisherArticleExist.publishArticleExist(articleId, correlationId);
//
//            // Espera la respuesta asociada al correlationId
//            Boolean isValid = articleExistResponseConsumer.waitForResponse(correlationId);
//           
//            // Si no llega respuesta, marcar como inválido
//            if (isValid == null) {
//                isValid = false;
//            }
//
//            // Actualiza el estado del artículo en la base de datos
//            //updateArticleStatus(profileId, articleId, isValid);
//        } catch (Exception e) {
//            // Si hay un error, marca el artículo como INVALID
//            //updateArticleStatus(profileId, articleId, false);
//            System.err.println("Error durante la validación: " + e.getMessage());
//        }
//    }
    


    public void addArticleToWishlist(String profileId, String articleId) {
        // Verifica si el artículo ya está en la wishlist
        Optional<WishlistItem> existingItem = wishlistRepository.findByProfileIdAndArticleId(profileId, articleId);
        if (existingItem.isPresent()) {
            throw new IllegalArgumentException("El artículo ya está en la wishlist.");
        }

        // Verifica que exista un perfil
        if (!profileService.existProfileByID(profileId)) {
            throw new IllegalArgumentException("No existe un perfil al que asignar el artículo.");
        }
        
        String correlationId = java.util.UUID.randomUUID().toString();
        System.out.println(correlationId);
        
        publisherArticleExist.publishArticleExist(articleId, correlationId);
        
        // Crea un nuevo artículo con estado PENDING_VALIDATION
        WishlistItem newItem = new WishlistItem(profileId, articleId,correlationId);
        wishlistRepository.save(newItem);

        
    }
    
    
    public void deletArticleWishlist(String profileId, String articleId){
        
        if (!profileService.existProfileByID(profileId)) {
            throw new IllegalArgumentException("No existe un perfil al que asignar el artículo.");
        }
        System.out.println("profileId:"+profileId);
        System.out.println("articleId:"+articleId);
        Optional<WishlistItem> existingItem = wishlistRepository.findByProfileIdAndArticleId(profileId, articleId);
        
        if (!existingItem.isPresent()) {
            throw new IllegalArgumentException("El articulo que quieres eliminar no esta en la wishList");
        }
       
        
        
        WishlistItem wishlistItem =  existingItem.get();
        
        wishlistRepository.delete(wishlistItem);
        
    }
    

    public void updateArticleStatus(String correlationId, boolean isValid) {
        WishlistItem item = wishlistRepository.findByCorrelationId(correlationId)
                .orElseThrow(() -> new IllegalArgumentException("El artículo no existe en la wishlist."));
        
        if (isValid) {
            item.setStatus(WishlistStatus.VALID);
        } else {
            item.setStatus(WishlistStatus.INVALID);
        }
        
        wishlistRepository.save(item);
        
    }
    
    
    public List<WishlistItem> findWishListItemByProfileId(String profileId){
        return wishlistRepository.findByProfileIdAndStatus(profileId, WishlistStatus.VALID);
    }
}
