/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.rest;

import com.microservice.amin.security.TokenService;
import com.microservice.amin.rest.dto.ProfileRequest;
import com.microservice.amin.profile.Profile;
import com.microservice.amin.profile.ProfileService;
import com.microservice.amin.security.User;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author cuent
 */
@RestController
public class PostProfile {

    private static final Logger logger = LoggerFactory.getLogger(PostProfile.class);

    @Autowired
    TokenService tokenService;

    @Autowired
    ProfileService service;

    @PostMapping("/v1/profile")
    public ResponseEntity<Map<String, Object>> postProfile(
            @RequestHeader("Authorization") String auth,
            @RequestBody ProfileRequest profileRequest) {

        logger.info("Recibida solicitud para crear/actualizar perfil");

        User user = tokenService.validateUser(auth);
        logger.debug("Usuario validado exitosamente: {}", user.getId());

        // Buscar perfil existente del usuario
        Profile existingProfile = service.findProfileByUserID(user.getId());

        Profile profile;
        if (existingProfile != null) {
            // Actualizar perfil existente
            profile = existingProfile;
            profile.setFirstName(profileRequest.getFirstName());
            profile.setLastName(profileRequest.getLastName());
            profile.setEmail(profileRequest.getEmail());
            profile.setImageId(profileRequest.getImageId());
            profile.setUpdateDate(new Date());
        } else {
            // Crear nuevo perfil
            profile = new Profile();
            profile.setUserId(user.id);
            profile.setFirstName(profileRequest.getFirstName());
            profile.setLastName(profileRequest.getLastName());
            profile.setEmail(profileRequest.getEmail());
            profile.setCreationDate(new Date());
            profile.setImageId(profileRequest.getImageId());
        }

        Profile savedProfile = service.createOrUpdateProfile(profile, auth);

        // Crear respuesta con todos los datos del perfil
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Perfil procesado exitosamente");
        response.put("status", "success");
        response.put("data", createProfileData(savedProfile));

        logger.info("Perfil procesado exitosamente. ProfileId: {}", savedProfile.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un mapa con todos los datos del perfil para la respuesta
     * 
     * @param profile El perfil a incluir en la respuesta
     * @return Mapa con los datos del perfil
     */
    private Map<String, Object> createProfileData(Profile profile) {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", profile.getId());
        profileData.put("userId", profile.getUserId());
        profileData.put("firstName", profile.getFirstName());
        profileData.put("lastName", profile.getLastName());
        profileData.put("email", profile.getEmail());
        profileData.put("imageId", profile.getImageId());
        profileData.put("creationDate", profile.getCreationDate());
        profileData.put("updateDate", profile.getUpdateDate());

        return profileData;
    }

}
