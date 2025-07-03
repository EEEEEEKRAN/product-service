package com.microcommerce.productservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderEvent {
    private Long orderId;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemEvent> orderItems;
    private String eventType; // ORDER_CREATED, ORDER_STATUS_UPDATED, ORDER_CANCELLED, ORDER_DELETED
    private LocalDateTime timestamp;
    
    // Constructeurs
    public OrderEvent() {
        this.timestamp = LocalDateTime.now();
    }
    
    public OrderEvent(Long orderId, Long userId, String status, BigDecimal totalAmount, 
                     List<OrderItemEvent> orderItems, String eventType) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderItems = orderItems;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }
    
    // Classe interne pour les items de commande
    public static class OrderItemEvent {
        private String productId;
        private Integer quantity;
        private BigDecimal price;
        
        public OrderItemEvent() {}
        
        public OrderItemEvent(String productId, Integer quantity, BigDecimal price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }
        
        // Getters et Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }
    
    // Getters et Setters
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public List<OrderItemEvent> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItemEvent> orderItems) {
        this.orderItems = orderItems;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "OrderEvent{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", orderItems=" + (orderItems != null ? orderItems.size() : 0) + " items" +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}