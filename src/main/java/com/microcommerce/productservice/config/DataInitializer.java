package com.microcommerce.productservice.config;

import com.microcommerce.productservice.entity.Product;
import com.microcommerce.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Initialise quelques données de test dans MongoDB au démarrage
 * Pratique pour avoir des données dès le début
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // On vérifie si on a déjà des données
        if (productRepository.count() == 0) {
            logger.info("Initialisation des données de test...");
            
            // Créer quelques produits de démo
            createProduct("iPhone 15 Pro", "Smartphone Apple dernière génération avec puce A17 Pro", 
                         new BigDecimal("1199.99"), 25, "Smartphones");
            
            createProduct("Samsung Galaxy S24", "Smartphone Samsung avec écran Dynamic AMOLED 2X", 
                         new BigDecimal("899.99"), 30, "Smartphones");
            
            createProduct("MacBook Air M3", "Ordinateur portable Apple avec puce M3, 13 pouces", 
                         new BigDecimal("1299.99"), 15, "Ordinateurs");
            
            createProduct("Dell XPS 13", "Ultrabook Dell avec écran InfinityEdge", 
                         new BigDecimal("999.99"), 20, "Ordinateurs");
            
            createProduct("AirPods Pro 2", "Écouteurs sans fil Apple avec réduction de bruit active", 
                         new BigDecimal("279.99"), 50, "Audio");
            
            createProduct("Sony WH-1000XM5", "Casque sans fil Sony avec réduction de bruit", 
                         new BigDecimal("399.99"), 35, "Audio");
            
            createProduct("iPad Air", "Tablette Apple avec puce M1, 10.9 pouces", 
                         new BigDecimal("699.99"), 40, "Tablettes");
            
            createProduct("Nintendo Switch OLED", "Console de jeu portable Nintendo avec écran OLED", 
                         new BigDecimal("349.99"), 45, "Gaming");
            
            createProduct("PlayStation 5", "Console de jeu Sony nouvelle génération", 
                         new BigDecimal("549.99"), 8, "Gaming");
            
            createProduct("Logitech MX Master 3S", "Souris sans fil ergonomique pour professionnels", 
                         new BigDecimal("109.99"), 75, "Accessoires");
            
            logger.info("Données de test initialisées avec succès ! {} produits créés", productRepository.count());
        } else {
            logger.info("Base de données déjà initialisée avec {} produits", productRepository.count());
        }
    }
    
    private void createProduct(String name, String description, BigDecimal price, Integer stock, String category) {
        Product product = new Product(name, description, price, stock, category);
        productRepository.save(product);
    }
}