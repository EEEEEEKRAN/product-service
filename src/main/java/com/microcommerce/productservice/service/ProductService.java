package com.microcommerce.productservice.service;

import com.microcommerce.productservice.entity.Product;
import com.microcommerce.productservice.dto.ProductInfoDto;
import com.microcommerce.productservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Notre service pour gérer les produits
 * C'est ici qu'on met toute la logique métier, les validations et tout le bazar
 */
@Service
@Transactional
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductEventPublisher productEventPublisher;
    
    /**
     * Récupère tous les produits qu'on a
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        logger.debug("Récupération de tous les produits");
        return productRepository.findAll();
    }
    
    /**
     * Chope un produit par son ID
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(String id) {
        logger.debug("Récupération du produit avec l'ID: {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Récupère les infos d'un produit pour les autres services (format allégé)
     */
    @Transactional(readOnly = true)
    public ProductInfoDto getProductInfoDto(String id) {
        logger.debug("Récupération des infos produit pour service - ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + id));
        return convertToProductInfoDto(product);
    }
    
    /**
     * Crée un nouveau produit (avec validation et événement RabbitMQ)
     */
    public Product createProduct(Product product) {
        // Validation métier - on vérifie que tout est correct
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix doit être positif");
        }
        
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Le stock ne peut pas être négatif");
        }
        
        logger.info("Création du produit: {}", product.getName());
        Product savedProduct = productRepository.save(product);
        
        // On balance l'événement de création dans RabbitMQ
        productEventPublisher.publishProductCreated(savedProduct);
        
        return savedProduct;
    }
    
    /**
     * Met à jour un produit existant
     */
    public Product updateProduct(String id, Product productDetails) {
        logger.info("Mise à jour du produit ID: {}", id);
        
        return productRepository.findById(id)
                .map(existingProduct -> {
                    // On vérifie que le nouveau nom n'est pas déjà pris par un autre produit, sinon c'est chiant
                    if (!existingProduct.getName().equalsIgnoreCase(productDetails.getName()) &&
                        productRepository.existsByNameIgnoreCase(productDetails.getName())) {
                        throw new IllegalArgumentException("Un autre produit avec ce nom existe déjà: " + productDetails.getName());
                    }
                    
                    // On met à jour tous les champs
                    existingProduct.setName(productDetails.getName());
                    existingProduct.setDescription(productDetails.getDescription());
                    existingProduct.setPrice(productDetails.getPrice());
                    existingProduct.setStock(productDetails.getStock());
                    existingProduct.setCategory(productDetails.getCategory());
                    
                    Product updatedProduct = productRepository.save(existingProduct);
                    logger.info("Produit mis à jour avec succès: {}", updatedProduct.getName());
                    
                    // On balance l'événement de mise à jour dans RabbitMQ
                    productEventPublisher.publishProductUpdated(updatedProduct);
                    
                    return updatedProduct;
                })
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + id));
    }
    
    /**
     * Supprime un produit, ouais voila, c'est tout lol
     */
    public void deleteProduct(String id) {
        logger.info("Suppression du produit ID: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Produit non trouvé avec l'ID: " + id);
        }
        
        productRepository.deleteById(id);
        logger.info("Produit supprimé avec succès, ID: {}", id);
        
        // On balance l'événement de suppression dans RabbitMQ
        productEventPublisher.publishProductDeleted(id);
    }
    
    /**
     * Recherche des produits par nom
     */
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        logger.debug("Recherche de produits contenant: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Récupère tous les produits d'une catégorie
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        logger.debug("Récupération des produits de la catégorie: {}", category);
        return productRepository.findByCategory(category);
    }
    
    /**
     * Récupère les produits dans une fourchette de prix
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        logger.debug("Recherche de produits entre {} et {} euros", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    /**
     * Récupère seulement les produits qu'on a encore en stock
     */
    @Transactional(readOnly = true)
    public List<Product> getAvailableProducts() {
        logger.debug("Récupération des produits en stock");
        return productRepository.findByStockGreaterThan(0);
    }
    
    /**
     * Récupère les produits qui commencent à manquer
     */
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        logger.debug("Récupération des produits avec stock <= {}", threshold);
        return productRepository.findLowStockProducts(threshold);
    }
    
    /**
     * Met à jour juste le stock d'un produit
     */
    public Product updateStock(String id, Integer newStock) {
        logger.info("Mise à jour du stock pour le produit ID: {}, nouveau stock: {}", id, newStock);
        
        return productRepository.findById(id)
                .map(product -> {
                    product.setStock(newStock);
                    Product updatedProduct = productRepository.save(product);
                    logger.info("Stock mis à jour pour le produit: {}", updatedProduct.getName());
                    
                    // On balance l'événement de mise à jour dans RabbitMQ (changement de stock)
                    productEventPublisher.publishProductUpdated(updatedProduct);
                    
                    return updatedProduct;
                })
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + id));
    }
    
    /**
     * Recherche full-text dans les produits (nom + description)
     */
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        logger.debug("Recherche full-text avec le mot-clé: {}", keyword);
        return productRepository.searchByKeyword(keyword);
    }
    
    /**
     * Convertit une entité Product en ProductInfoDto (pour les appels inter-services)
     */
    private ProductInfoDto convertToProductInfoDto(Product product) {
        return new ProductInfoDto(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getCategory(),
            product.getStock()
        );
    }
}