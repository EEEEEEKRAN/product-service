package com.microcommerce.productservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);
    
    @RabbitListener(queues = "product-service.user.queue")
    public void handleUserEvent(UserEvent userEvent) {
        logger.info("Événement utilisateur reçu dans product-service: {}", userEvent);
        
        try {
            switch (userEvent.getEventType()) {
                case "USER_CREATED":
                    handleUserCreated(userEvent);
                    break;
                case "USER_UPDATED":
                    handleUserUpdated(userEvent);
                    break;
                case "USER_DELETED":
                    handleUserDeleted(userEvent);
                    break;
                default:
                    logger.warn("Type d'événement utilisateur non géré: {}", userEvent.getEventType());
            }
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de l'événement utilisateur: {}", userEvent, e);
        }
    }
    
    private void handleUserCreated(UserEvent userEvent) {
        logger.info("Utilisateur créé - ID: {}, Nom: {}, Email: {}", 
                   userEvent.getUserId(), userEvent.getName(), userEvent.getEmail());
        
        // Ici on pourrait initialiser des données spécifiques à l'utilisateur
        // comme des recommandations de produits ou des préférences
    }
    
    private void handleUserUpdated(UserEvent userEvent) {
        logger.info("Utilisateur mis à jour - ID: {}, Nom: {}, Email: {}", 
                   userEvent.getUserId(), userEvent.getName(), userEvent.getEmail());
        
        // Ici on pourrait mettre à jour des caches ou des recommandations
        // basées sur les nouvelles informations utilisateur
    }
    
    private void handleUserDeleted(UserEvent userEvent) {
        logger.info("Utilisateur supprimé - ID: {}", userEvent.getUserId());
        
        // Ici on pourrait nettoyer des données associées à l'utilisateur
        // comme des recommandations personnalisées ou des historiques
        logger.info("Nettoyage des données utilisateur {} dans le service produit", 
                   userEvent.getUserId());
    }
}