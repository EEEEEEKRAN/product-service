package com.microcommerce.productservice.dto;

import java.math.BigDecimal;

/**
 * DTO optimisé pour la communication inter-services
 * 
 * Contient uniquement les infos essentielles d'un produit
 * pour les appels entre microservices (pas de description complète)
 */
public class ProductInfoDto {
    
    private String id;
    private String name;
    private BigDecimal price;
    private String category;
    private Integer stock;
    private boolean available;
    
    // Constructeurs
    public ProductInfoDto() {}
    
    public ProductInfoDto(String id, String name, BigDecimal price, String category, Integer stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.available = stock != null && stock > 0;
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
        this.available = stock != null && stock > 0;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    @Override
    public String toString() {
        return "ProductInfoDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", stock=" + stock +
                ", available=" + available +
                '}';
    }
}