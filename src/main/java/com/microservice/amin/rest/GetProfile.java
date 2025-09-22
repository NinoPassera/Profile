/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rest;

import com.microservice.amin.profile.Profile;
import com.microservice.amin.profile.ProfileService;
import com.microservice.amin.rest.dto.ProfileResponseDTO;
import com.microservice.amin.security.User;
import com.microservice.amin.wish.WishlistItem;
import com.microservice.amin.wish.WishlistService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.microservice.amin.security.TokenService;

/**
 *
 * @author cuent
 */
@RestController
public class GetProfile {

    @Autowired
    TokenService tokenService;

    @Autowired
    ProfileService profileService;

    @Autowired
    WishlistService wishlistService;

    @GetMapping("/v1/profile")
    public ResponseEntity<ProfileResponseDTO> getProfile(
            @RequestHeader("Authorization") String auth) {

        User user = tokenService.validateUser(auth);
        System.out.println(user.getId());
        Profile profile = profileService.findProfileByUserID(user.getId());

        if (profile == null) {
            return ResponseEntity.notFound().build(); // 404 si el perfil no existe
        }

        List<WishlistItem> listWishItem = wishlistService.findWishListItemByProfileId(profile.getId());

        // Extraer los IDs de cada WishlistItem y almacenarlos en una lista de Strings
        List<String> listWishItemIds = listWishItem.stream()
                .map(WishlistItem::getId) // Usa el método getter para obtener el ID
                .collect(Collectors.toList());

        ProfileResponseDTO responseDTO = new ProfileResponseDTO(
                profile.id,
                profile.userId,
                profile.firstName + " " + profile.lastName, // Concatenar el nombre
                profile.email,
                listWishItemIds, // Deseo de lista simulada
                profile.imageId,
                profile.creationDate.getTime(), // Fecha como timestamp
                profile.updateDate.getTime());

        return ResponseEntity.ok(responseDTO);
    }
}
