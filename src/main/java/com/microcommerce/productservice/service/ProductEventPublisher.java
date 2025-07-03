package com.microcommerce.productservice.service;

import com.microcommerce.productservice.config.RabbitMQConfig;
import com.microcommerce.productservice.entity.Product;
import com.microcommerce.productservice.event.ProductEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service pour publier les événements produits vers RabbitMQ
 * 
 * Chaque fois qu'un produit est créé, modifié ou supprimé,
 * on envoie un event pour que les autres services se synchronisent.
 */
@Service
public class ProductEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductEventPublisher.class);
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * Publie un événement de création de produit
     */
    public void publishProductCreated(Product product) {
        ProductEvent event = createProductEvent(product, ProductEvent.EventType.CREATED);
        publishEvent(event, RabbitMQConfig.PRODUCT_CREATED_ROUTING_KEY);
        logger.info("Événement PRODUCT_CREATED publié pour le produit: {}", product.getId());
    }
    
    /**
     * Publie un événement de mise à jour de produit
     */
    public void publishProductUpdated(Product product) {
        ProductEvent event = createProductEvent(product, ProductEvent.EventType.UPDATED);
        publishEvent(event, RabbitMQConfig.PRODUCT_UPDATED_ROUTING_KEY);
        logger.info("Événement PRODUCT_UPDATED publié pour le produit: {}", product.getId());
    }
    
    /**
     * Publie un événement de suppression de produit
     */
    public void publishProductDeleted(String productId) {
        ProductEvent event = new ProductEvent();
        event.setProductId(productId);
        event.setEventType(ProductEvent.EventType.DELETED);
        
        publishEvent(event, RabbitMQConfig.PRODUCT_DELETED_ROUTING_KEY);
        logger.info("Événement PRODUCT_DELETED publié pour le produit: {}", productId);
    }
    
    /**
     * Crée un ProductEvent à partir d'une entité Product
     */
    private ProductEvent createProductEvent(Product product, ProductEvent.EventType eventType) {
        return new ProductEvent(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getCategory(),
            eventType
        );
    }
    
    /**
     * Envoie l'événement vers RabbitMQ
     */
    private void publishEvent(ProductEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.PRODUCT_EXCHANGE,
                routingKey,
                event
            );
            logger.debug("Événement envoyé avec succès: {}", event);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'événement: {}", event, e);
            // En production, on pourrait implémenter un retry ou stocker l'event pour retry plus tard
        }
    }
}