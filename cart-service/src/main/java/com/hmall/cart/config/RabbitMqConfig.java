package com.hmall.cart.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class RabbitMqConfig {

    public static final String ORDER_EXCHANGE = "trade.order";
    public static final String CART_QUEUE = "cart.clean";

    private final ConnectionFactory connectionFactory;

    public RabbitMqConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @PostConstruct
    public void init() {
        try {
            Connection conn = (Connection) connectionFactory.createConnection().getDelegate();
            Channel ch = conn.createChannel();

            ch.exchangeDeclare(ORDER_EXCHANGE, "direct", true);
            ch.queueDeclare(CART_QUEUE, true, false, false, null);
            ch.queueBind(CART_QUEUE, ORDER_EXCHANGE, "cart");

            log.info("cart-service：cart.clean 队列声明完成");
            ch.close();
        } catch (Exception e) {
            log.error("cart-service MQ 初始化失败", e);
        }
    }
}
