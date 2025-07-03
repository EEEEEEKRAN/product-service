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
 * Service pour la gestion des produits
 * Ici on met toute la logique métier, les validations, etc.
 */
@Service
@Transactional
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Récupérer tous les produits
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        logger.debug("Récupération de tous les produits");
        return productRepository.findAll();
    }
    
    /**
     * Récupérer un produit par ID
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(String id) {
        logger.debug("Récupération du produit avec l'ID: {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * Récupérer les infos d'un produit pour la communication inter-services
     */
    @Transactional(readOnly = true)
    public ProductInfoDto getProductInfoDto(String id) {
        logger.debug("Récupération des infos produit pour service - ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + id));
        return convertToProductInfoDto(product);
    }
    
    /**
     * Créer un nouveau produit
     */
    public Product createProduct(Product product) {
        // Validation business
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix doit être positif");
        }
        
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Le stock ne peut pas être négatif");
        }
        
        logger.info("Création du produit: {}", product.getName());
        return productRepository.save(product);
    }
    
    /**
     * Mettre à jour un produit existant
     */
    public Product updateProduct(String id, Product productDetails) {
        logger.info("Mise à jour du produit ID: {}", id);
        
        return productRepository.findById(id)
                .map(existingProduct -> {
                    // Vérifier si le nouveau nom n'est pas déjà pris par un autre produit
                    if (!existingProduct.getName().equalsIgnoreCase(productDetails.getName()) &&
                        productRepository.existsByNameIgnoreCase(productDetails.getName())) {
                        throw new IllegalArgumentException("Un autre produit avec ce nom existe déjà: " + productDetails.getName());
                    }
                    
                    // Mettre à jour les champs
                    existingProduct.setName(productDetails.getName());
                    existingProduct.setDescription(productDetails.getDescription());
                    existingProduct.setPrice(productDetails.getPrice());
                    existingProduct.setStock(productDetails.getStock());
                    existingProduct.setCategory(productDetails.getCategory());
                    
                    Product updatedProduct = productRepository.save(existingProduct);
                    logger.info("Produit mis à jour avec succès: {}", updatedProduct.getName());
                    return updatedProduct;
                })
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + id));
    }
    
    /**
     * Supprimer un produit
     */
    public void deleteProduct(String id) {
        logger.info("Suppression du produit ID: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Produit non trouvé avec l'ID: " + id);
        }
        
        productRepository.deleteById(id);
        logger.info("Produit supprimé avec succès, ID: {}", id);
    }
    
    /**
     * Rechercher des produits par nom
     */
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        logger.debug("Recherche de produits contenant: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Récupérer les produits par catégorie
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        logger.debug("Récupération des produits de la catégorie: {}", category);
        return productRepository.findByCategory(category);
    }
    
    /**
     * Récupérer les produits dans une fourchette de prix
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        logger.debug("Recherche de produits entre {} et {} euros", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    /**
     * Récupérer les produits avec stock disponible
     */
    @Transactional(readOnly = true)
    public List<Product> getAvailableProducts() {
        logger.debug("Récupération des produits en stock");
        return productRepository.findByStockGreaterThan(0);
    }
    
    /**
     * Récupérer les produits avec stock faible
     */
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        logger.debug("Récupération des produits avec stock <= {}", threshold);
        return productRepository.findLowStockProducts(threshold);
    }
    
    /**
     * Mettre à jour le stock d'un produit
     */
    public Product updateStock(String id, Integer newStock) {
        logger.info("Mise à jour du stock pour le produit ID: {}, nouveau stock: {}", id, newStock);
        
        return productRepository.findById(id)
                .map(product -> {
                    product.setStock(newStock);
                    Product updatedProduct = productRepository.save(product);
                    logger.info("Stock mis à jour pour le produit: {}", updatedProduct.getName());
                    return updatedProduct;
                })
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + id));
    }
    
    /**
     * Recherche full-text dans les produits
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