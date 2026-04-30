package com.smartqueue.task_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TASK_QUEUE_HIGH    = "task.queue.high";
    public static final String TASK_QUEUE_NORMAL  = "task.queue.normal";
    public static final String TASK_EXCHANGE      = "task.exchange";
    public static final String ROUTING_KEY_HIGH   = "task.high";
    public static final String ROUTING_KEY_NORMAL = "task.normal";

    // Queues
    @Bean
    public Queue highPriorityQueue() {
        return QueueBuilder.durable(TASK_QUEUE_HIGH).build();
    }

    @Bean
    public Queue normalPriorityQueue() {
        return QueueBuilder.durable(TASK_QUEUE_NORMAL).build();
    }

    // Exchange
    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(TASK_EXCHANGE);
    }

    // Bindings
    @Bean
    public Binding highPriorityBinding() {
        return BindingBuilder
                .bind(highPriorityQueue())
                .to(taskExchange())
                .with(ROUTING_KEY_HIGH);
    }

    @Bean
    public Binding normalPriorityBinding() {
        return BindingBuilder
                .bind(normalPriorityQueue())
                .to(taskExchange())
                .with(ROUTING_KEY_NORMAL);
    }

    // JSON Message Converter
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}