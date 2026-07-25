package com.hmall.trade.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@EnableRabbit
public class MqConfig {

    public static final String ORDER_EXCHANGE = "trade.order";
    public static final String STOCK_QUEUE = "stock.deduct";
    public static final String CART_QUEUE = "cart.clean";

    private final ConnectionFactory connectionFactory;

    public MqConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @PostConstruct
    public void init() {
        try {
            log.info("正在连接 RabbitMQ {}:5672...", "192.168.137.128");
            Connection conn = (Connection) connectionFactory.createConnection().getDelegate();
            Channel ch = conn.createChannel();
            ch.exchangeDeclare(ORDER_EXCHANGE, "direct", true);
            ch.queueDeclare(STOCK_QUEUE, true, false, false, null);
            ch.queueDeclare(CART_QUEUE, true, false, false, null);
            ch.queueBind(STOCK_QUEUE, ORDER_EXCHANGE, "stock");
            ch.queueBind(CART_QUEUE, ORDER_EXCHANGE, "cart");
            log.info("RabbitMQ 队列和交换机已创建");
            ch.close();
        } catch (Exception e) {
            log.error("RabbitMQ 初始化失败", e);
        }
    }
}
