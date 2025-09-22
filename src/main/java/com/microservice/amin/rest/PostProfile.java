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

        Profile profile = new Profile();
        profile.setUserId(user.id);
        profile.setFirstName(profileRequest.getFirstName());
        profile.setLastName(profileRequest.getLastName());
        profile.setEmail(profileRequest.getEmail());
        profile.setCreationDate(new Date());
        profile.setImageId(profileRequest.getImageId());

        Profile savedProfile = service.createOrUpdateProfile(profile, auth);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Perfil procesado exitosamente");
        response.put("profileId", savedProfile.getId());
        response.put("userId", savedProfile.getUserId());
        response.put("status", "success");

        logger.info("Perfil procesado exitosamente. ProfileId: {}", savedProfile.getId());
        return ResponseEntity.ok(response);
    }

}
