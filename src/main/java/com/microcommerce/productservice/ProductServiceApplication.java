package com.microcommerce.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe principale pour démarrer le Product Service
 * Rien de compliqué, juste le point d'entrée de notre microservice
 */
@SpringBootApplication
public class ProductServiceApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceApplication.class);
    
    public static void main(String[] args) {
        logger.info("🚀 Démarrage du Product Service...");
        SpringApplication.run(ProductServiceApplication.class, args);
        logger.info("✅ Product Service démarré avec succès!");
        logger.info("📋 API disponible sur: http://localhost:8081/api/products");
        logger.info("🗄️ Console H2 disponible sur: http://localhost:8081/api/products/h2-console");
    }
}