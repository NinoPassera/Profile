/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.wish;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author cuent
 */
@Repository
public interface WishlistRepository extends MongoRepository<WishlistItem, String> {

    List<WishlistItem> findByProfileIdAndStatus(String profileId, WishlistStatus status);

    Optional<WishlistItem> findByProfileIdAndArticleId(String profileId, String articleId);
    
    Optional<WishlistItem> findByCorrelationId(String correlationId);
    
    
    
}

