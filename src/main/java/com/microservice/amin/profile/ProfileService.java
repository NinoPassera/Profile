/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.amin.profile;

import com.microservice.amin.exception.ImageValidationException;
import com.microservice.amin.exception.ProfileValidationException;
import com.microservice.amin.rabbit.PublishProfile;
import com.microservice.amin.rabbit.dto.PublishProfileDataEvent;
import com.microservice.amin.security.TokenService;
import com.microservice.amin.tools.Validations;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author cuent
 */
@Service
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    @Value("${rabbitmq.exchange.profile}")
    private String profileExchange;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PublishProfile publishProfile;

    @Autowired
    TokenService tokenService;

    @Autowired
    Validations validations;

    public Profile createOrUpdateProfile(Profile profile, String auth) {
        // Validaciones de entrada
        validateInput(profile, auth);

        logger.info("Iniciando creación/actualización de perfil para userId: {}",
                profile.getUserId());

        Profile existingProfile = profileRepository.findByUserId(profile.getUserId());

        if (existingProfile != null) {
            logger.info("Perfil existente encontrado, procediendo con actualización. ProfileId: {}",
                    existingProfile.getId());
            return updateExistingProfile(existingProfile, profile, auth);
        } else {
            logger.info("No se encontró perfil existente, procediendo con creación para userId: {}",
                    profile.getUserId());
            return createNewProfile(profile, auth);
        }
    }

    private void validateInput(Profile profile, String auth) {
        if (profile == null) {
            logger.error("El perfil no puede ser null");
            throw new ProfileValidationException("El perfil no puede ser null");
        }

        if (auth == null || auth.trim().isEmpty()) {
            logger.error("El token de autorización no puede ser null o vacío");
            throw new ProfileValidationException("El token de autorización es requerido");
        }

        if (profile.getUserId() == null || profile.getUserId().trim().isEmpty()) {
            logger.error("El userId no puede ser null o vacío");
            throw new ProfileValidationException("El userId es requerido");
        }

        if (profile.getFirstName() == null || profile.getFirstName().trim().isEmpty()) {
            logger.error("El firstName no puede ser null o vacío");
            throw new ProfileValidationException("El firstName es requerido");
        }

        if (profile.getLastName() == null || profile.getLastName().trim().isEmpty()) {
            logger.error("El lastName no puede ser null o vacío");
            throw new ProfileValidationException("El lastName es requerido");
        }

        if (profile.getEmail() == null || profile.getEmail().trim().isEmpty()) {
            logger.error("El email no puede ser null o vacío");
            throw new ProfileValidationException("El email es requerido");
        }

        if (!isValidEmailFormat(profile.getEmail())) {
            logger.error("El formato del email no es válido: {}", profile.getEmail());
            throw new ProfileValidationException("El formato del email no es válido");
        }
    }

    private Profile updateExistingProfile(Profile existingProfile, Profile newProfileData, String auth) {
        // Validar imagen solo si cambió
        if (shouldValidateImage(existingProfile.getImageId(), newProfileData.getImageId())) {
            validateImage(newProfileData.getImageId(), auth);
        }

        // Actualizar campos
        existingProfile.setFirstName(newProfileData.getFirstName());
        existingProfile.setLastName(newProfileData.getLastName());
        existingProfile.setEmail(newProfileData.getEmail());
        existingProfile.setImageId(newProfileData.getImageId());
        existingProfile.setUpdateDate(new Date());

        Profile savedProfile = profileRepository.save(existingProfile);
        publishProfileEvent(savedProfile);

        logger.info("Perfil actualizado exitosamente. ProfileId: {}", savedProfile.getId());
        return savedProfile;
    }

    private Profile createNewProfile(Profile profile, String auth) {
        // Validar imagen si se proporciona
        if (profile.getImageId() != null && !profile.getImageId().trim().isEmpty()) {
            validateImage(profile.getImageId(), auth);
        }

        Profile newProfile = new Profile(
                profile.getUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getEmail(),
                profile.getImageId());

        Profile savedProfile = profileRepository.save(newProfile);
        publishProfileEvent(savedProfile);

        logger.info("Perfil creado exitosamente. ProfileId: {}, UserId: {}",
                savedProfile.getId(), savedProfile.getUserId());
        return savedProfile;
    }

    private boolean shouldValidateImage(String existingImageId, String newImageId) {
        return newImageId != null &&
                !newImageId.trim().isEmpty() &&
                (existingImageId == null || !existingImageId.equals(newImageId));
    }

    private void validateImage(String imageId, String auth) {
        logger.debug("Validando imagen con imageId: {}", imageId);

        if (!validations.validarImagen(imageId, auth)) {
            logger.error("Validación de imagen fallida para imageId: {}", imageId);
            throw new ImageValidationException(
                    "La imagen que se quiere agregar no está almacenada en el servicio.");
        }

        logger.debug("Imagen validada exitosamente para imageId: {}", imageId);
    }

    private void publishProfileEvent(Profile profile) {
        try {
            PublishProfileDataEvent event = new PublishProfileDataEvent(
                    profile.getId(),
                    profile.getUserId(),
                    profile.getFirstName(),
                    profile.getLastName(),
                    profile.getImageId(),
                    profile.getCreationDate(),
                    profile.getUpdateDate());

            publishProfile.publish(profileExchange, event);
            logger.debug("Evento de perfil publicado exitosamente para ProfileId: {} en exchange: {}",
                    profile.getId(), profileExchange);
        } catch (Exception e) {
            logger.error("Error al publicar evento de perfil para ProfileId: {} en exchange: {}",
                    profile.getId(), profileExchange, e);
            // No lanzamos excepción aquí para no fallar la operación principal
        }
    }

    public Profile getProfile(String profileId) {
        return profileRepository.findById(profileId).orElse(null);
    }

    public Profile findProfileByUserID(String userId) {
        return profileRepository.findByUserId(userId);
    }

    public boolean existProfileByID(String id) {
        return profileRepository.existsById(id);
    }

    /**
     * Valida el formato del email usando expresión regular
     * 
     * @param email El email a validar
     * @return true si el formato es válido, false en caso contrario
     */
    private boolean isValidEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Expresión regular para validar formato de email
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        try {
            return email.trim().matches(emailRegex);
        } catch (Exception e) {
            logger.warn("Error al validar formato de email: {}", email, e);
            return false;
        }
    }

}
