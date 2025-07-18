package com.microcommerce.productservice.controller;

import com.microcommerce.productservice.entity.Product;
import com.microcommerce.productservice.dto.ProductInfoDto;
import com.microcommerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Notre controller REST pour gérer les produits
 * Tout ce qu'il faut pour faire du CRUD sur nos produits, rien de plus
 */
@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*") // On autorise tout le monde pour les tests, à changer en prod !
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    @Autowired
    private ProductService productService;
    
    /**
     * GET / - Récupère tous les produits qu'on a en base
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        logger.info("Demande de récupération de tous les produits");
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    /**
     * GET /{id} - Chope un produit spécifique par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        logger.info("Demande de récupération du produit ID: {}", id);
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(product))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * POST / - Crée un nouveau produit dans notre catalogue
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product) {
        logger.info("Demande de création d'un produit: {}", product.getName());
        try {
            Product createdProduct = productService.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (IllegalArgumentException e) {
            logger.warn("Erreur lors de la création du produit: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * PUT /{id} - Met à jour un produit existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @Valid @RequestBody Product productDetails) {
        logger.info("Demande de mise à jour du produit ID: {}", id);
        try {
            Product updatedProduct = productService.updateProduct(id, productDetails);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            logger.warn("Erreur de validation lors de la mise à jour: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            logger.warn("Erreur lors de la mise à jour du produit: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * DELETE /{id} - Supprime un produit (bye bye !)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        logger.info("Demande de suppression du produit ID: {}", id);
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "Produit supprimé avec succès"));
        } catch (RuntimeException e) {
            logger.warn("Erreur lors de la suppression: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /search?name={name} - Recherche des produits par nom ou mot-clé
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String keyword) {
        
        if (name != null && !name.trim().isEmpty()) {
            logger.info("Recherche de produits par nom: {}", name);
            List<Product> products = productService.searchProductsByName(name);
            return ResponseEntity.ok(products);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            logger.info("Recherche full-text avec le mot-clé: {}", keyword);
            List<Product> products = productService.searchProducts(keyword);
            return ResponseEntity.ok(products);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * GET /category/{category} - Tous les produits d'une catégorie donnée
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        logger.info("Récupération des produits de la catégorie: {}", category);
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
    
    /**
     * GET /price-range?min={min}&max={max} - Produits dans une fourchette de prix (pratique nan ?)
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        logger.info("Recherche de produits entre {} et {} euros", min, max);
        List<Product> products = productService.getProductsByPriceRange(min, max);
        return ResponseEntity.ok(products);
    }
    
    /**
     * GET /available - Seulement les produits qu'on a encore en stock
     */
    @GetMapping("/available")
    public ResponseEntity<List<Product>> getAvailableProducts() {
        logger.info("Récupération des produits disponibles");
        List<Product> products = productService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }
    
    /**
     * GET /low-stock?threshold={threshold} - Produits qui commencent à manquer
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @RequestParam(defaultValue = "5") Integer threshold) {
        logger.info("Récupération des produits avec stock faible (seuil: {})", threshold);
        List<Product> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
    
    /**
     * PATCH /{id}/stock - Met à jour juste le stock d'un produit
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable String id, @RequestBody Map<String, Integer> stockUpdate) {
        logger.info("Mise à jour du stock pour le produit ID: {}", id);
        try {
            Integer newStock = stockUpdate.get("stock");
            if (newStock == null || newStock < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Stock invalide"));
            }
            
            Product updatedProduct = productService.updateStock(id, newStock);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            logger.warn("Erreur lors de la mise à jour du stock: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /internal/{id} - Endpoint interne pour les autres services
     * Retourne juste les infos essentielles (optimisé pour les appels entre microservices)
     */
    @GetMapping("/internal/{id}")
    public ResponseEntity<ProductInfoDto> getProductInfoForService(@PathVariable String id) {
        logger.info("Demande d'infos produit pour service - ID: {}", id);
        try {
            ProductInfoDto productInfo = productService.getProductInfoDto(id);
            return ResponseEntity.ok(productInfo);
        } catch (RuntimeException e) {
            logger.warn("Produit non trouvé pour service - ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /health - Endpoint de santé du service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        long productCount = productService.getAllProducts().size();
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "product-service",
                "productCount", productCount,
                "timestamp", System.currentTimeMillis()
        ));
    }
}