package com.microcommerce.productservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ pour le service produit
 * 
 * On définit ici les exchanges, queues et bindings pour la communication
 * entre microservices. Quand un produit change, on envoie un event.
 */
@Configuration
public class RabbitMQConfig {

    // Noms des exchanges et queues
    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String PRODUCT_CREATED_QUEUE = "product.created.queue";
    public static final String PRODUCT_UPDATED_QUEUE = "product.updated.queue";
    public static final String PRODUCT_DELETED_QUEUE = "product.deleted.queue";
    
    // Routing keys pour diriger les messages
    public static final String PRODUCT_CREATED_ROUTING_KEY = "product.created";
    public static final String PRODUCT_UPDATED_ROUTING_KEY = "product.updated";
    public static final String PRODUCT_DELETED_ROUTING_KEY = "product.deleted";

    /**
     * Exchange principal pour les événements produits
     * Type Topic pour pouvoir router selon les routing keys
     */
    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(PRODUCT_EXCHANGE);
    }
    
    // Configuration pour écouter les événements commandes
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_ALL_ROUTING_KEY = "order.*";
    public static final String PRODUCT_SERVICE_ORDER_QUEUE = "product-service.order.queue";
    
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }
    
    @Bean
    public Queue productServiceOrderQueue() {
        return new Queue(PRODUCT_SERVICE_ORDER_QUEUE, true);
    }
    
    @Bean
    public Binding productServiceOrderBinding() {
        return BindingBuilder
            .bind(productServiceOrderQueue())
            .to(orderExchange())
            .with(ORDER_ALL_ROUTING_KEY);
    }
    
    // Configuration pour écouter les événements utilisateurs
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String USER_ALL_ROUTING_KEY = "user.*";
    public static final String PRODUCT_SERVICE_USER_QUEUE = "product-service.user.queue";
    
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }
    
    @Bean
    public Queue productServiceUserQueue() {
        return new Queue(PRODUCT_SERVICE_USER_QUEUE, true);
    }
    
    @Bean
    public Binding productServiceUserBinding() {
        return BindingBuilder
            .bind(productServiceUserQueue())
            .to(userExchange())
            .with(USER_ALL_ROUTING_KEY);
    }

    /**
     * Queue pour les événements de création de produit
     */
    @Bean
    public Queue productCreatedQueue() {
        return QueueBuilder.durable(PRODUCT_CREATED_QUEUE).build();
    }

    /**
     * Queue pour les événements de mise à jour de produit
     */
    @Bean
    public Queue productUpdatedQueue() {
        return QueueBuilder.durable(PRODUCT_UPDATED_QUEUE).build();
    }

    /**
     * Queue pour les événements de suppression de produit
     */
    @Bean
    public Queue productDeletedQueue() {
        return QueueBuilder.durable(PRODUCT_DELETED_QUEUE).build();
    }

    /**
     * Binding pour lier la queue de création à l'exchange
     */
    @Bean
    public Binding productCreatedBinding() {
        return BindingBuilder
                .bind(productCreatedQueue())
                .to(productExchange())
                .with(PRODUCT_CREATED_ROUTING_KEY);
    }

    /**
     * Binding pour lier la queue de mise à jour à l'exchange
     */
    @Bean
    public Binding productUpdatedBinding() {
        return BindingBuilder
                .bind(productUpdatedQueue())
                .to(productExchange())
                .with(PRODUCT_UPDATED_ROUTING_KEY);
    }

    /**
     * Binding pour lier la queue de suppression à l'exchange
     */
    @Bean
    public Binding productDeletedBinding() {
        return BindingBuilder
                .bind(productDeletedQueue())
                .to(productExchange())
                .with(PRODUCT_DELETED_ROUTING_KEY);
    }

    /**
     * Convertisseur JSON pour sérialiser/désérialiser les messages
     * Plus pratique que le sérialiseur par défaut
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Template RabbitMQ avec le convertisseur JSON
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}