package com.hmall.trade.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    public static final String ORDER_EXCHANGE = "trade.order";
    public static final String STOCK_QUEUE = "stock.deduct";
    public static final String CART_QUEUE = "cart.clean";

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue stockQueue() {
        return QueueBuilder.durable(STOCK_QUEUE).build();
    }

    @Bean
    public Queue cartQueue() {
        return QueueBuilder.durable(CART_QUEUE).build();
    }

    @Bean
    public Binding stockBinding(DirectExchange orderExchange, Queue stockQueue) {
        return BindingBuilder.bind(stockQueue).to(orderExchange).with("stock");
    }

    @Bean
    public Binding cartBinding(DirectExchange orderExchange, Queue cartQueue) {
        return BindingBuilder.bind(cartQueue).to(orderExchange).with("cart");
    }
}
