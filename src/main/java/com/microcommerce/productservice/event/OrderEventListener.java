package com.microcommerce.productservice.event;

import com.microcommerce.productservice.entity.Product;
import com.microcommerce.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @RabbitListener(queues = "product-service.order.queue")
    public void handleOrderEvent(OrderEvent orderEvent) {
        logger.info("Événement commande reçu dans product-service: {}", orderEvent);
        
        try {
            switch (orderEvent.getEventType()) {
                case "ORDER_CREATED":
                    handleOrderCreated(orderEvent);
                    break;
                case "ORDER_CANCELLED":
                    handleOrderCancelled(orderEvent);
                    break;
                case "ORDER_DELETED":
                    handleOrderDeleted(orderEvent);
                    break;
                case "ORDER_STATUS_UPDATED":
                    handleOrderStatusUpdated(orderEvent);
                    break;
                default:
                    logger.warn("Type d'événement commande non géré: {}", orderEvent.getEventType());
            }
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de l'événement commande: {}", orderEvent, e);
        }
    }
    
    private void handleOrderCreated(OrderEvent orderEvent) {
        logger.info("Commande créée - ID: {}, mise à jour des stocks", orderEvent.getOrderId());
        
        // Décrémenter le stock pour chaque produit commandé
        if (orderEvent.getOrderItems() != null) {
            for (OrderEvent.OrderItemEvent item : orderEvent.getOrderItems()) {
                updateProductStock(item.getProductId(), -item.getQuantity(), "Commande créée");
            }
        }
    }
    
    private void handleOrderCancelled(OrderEvent orderEvent) {
        logger.info("Commande annulée - ID: {}, restauration des stocks", orderEvent.getOrderId());
        
        // Restaurer le stock pour chaque produit de la commande annulée
        if (orderEvent.getOrderItems() != null) {
            for (OrderEvent.OrderItemEvent item : orderEvent.getOrderItems()) {
                updateProductStock(item.getProductId(), item.getQuantity(), "Commande annulée");
            }
        }
    }
    
    private void handleOrderDeleted(OrderEvent orderEvent) {
        logger.info("Commande supprimée - ID: {}, restauration des stocks", orderEvent.getOrderId());
        
        // Restaurer le stock pour chaque produit de la commande supprimée
        if (orderEvent.getOrderItems() != null) {
            for (OrderEvent.OrderItemEvent item : orderEvent.getOrderItems()) {
                updateProductStock(item.getProductId(), item.getQuantity(), "Commande supprimée");
            }
        }
    }
    
    private void handleOrderStatusUpdated(OrderEvent orderEvent) {
        logger.info("Statut de commande mis à jour - ID: {}, Statut: {}", 
                   orderEvent.getOrderId(), orderEvent.getStatus());
        
        // Ici on pourrait gérer des cas spécifiques selon le statut
        // Par exemple, si le statut passe à "DELIVERED", on pourrait mettre à jour des métriques
    }
    
    private void updateProductStock(String productId, Integer quantityChange, String reason) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                int newStock = product.getStock() + quantityChange;
                
                if (newStock < 0) {
                    logger.warn("Stock négatif détecté pour le produit {} après {}: nouveau stock = {}", 
                               productId, reason, newStock);
                }
                
                product.setStock(newStock);
                productRepository.save(product);
                
                logger.info("Stock mis à jour pour le produit {} ({}): {} -> {} ({})", 
                           productId, product.getName(), 
                           product.getStock() - quantityChange, newStock, reason);
            } else {
                logger.warn("Produit non trouvé pour la mise à jour de stock: ID = {}", productId);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du stock pour le produit {}: {}", 
                        productId, e.getMessage(), e);
        }
    }
}