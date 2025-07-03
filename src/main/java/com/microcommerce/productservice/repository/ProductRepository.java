package com.microcommerce.productservice.repository;

import com.microcommerce.productservice.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour les produits
 * Spring Data MongoDB génère automatiquement les implémentations
 */
@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    
    // Recherche par nom (case insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Recherche par catégorie
    List<Product> findByCategory(String category);
    
    // Produits avec stock disponible
    List<Product> findByStockGreaterThan(Integer stock);
    
    // Produits dans une fourchette de prix
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Recherche par catégorie et stock disponible
    List<Product> findByCategoryAndStockGreaterThan(String category, Integer stock);
    
    // Vérifier si un produit existe par nom (pour éviter les doublons)
    boolean existsByNameIgnoreCase(String name);
    
    // Recherche par nom exact (case insensitive)
    Optional<Product> findByNameIgnoreCase(String name);
    
    // Requête custom pour les produits populaires (stock faible)
    @Query("{ 'stock' : { $lte : ?0 } }")
    List<Product> findLowStockProducts(Integer threshold);
    
    // Compter les produits par catégorie
    @Query(value = "{ 'category' : ?0 }", count = true)
    Long countByCategory(String category);
    
    // Recherche full-text dans nom et description
    @Query("{ $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } } ] }")
    List<Product> searchByKeyword(String keyword);
}