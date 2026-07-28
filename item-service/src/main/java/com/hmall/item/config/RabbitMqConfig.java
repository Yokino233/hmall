package com.hmall.item.config;

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
    public static final String DLX_EXCHANGE = "trade.dlx";
    public static final String STOCK_QUEUE = "stock.deduct";
    public static final String STOCK_DLQ = "stock.deduct.dlq";

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

            // 声明死信交换机和死信队列
            ch.exchangeDeclare(DLX_EXCHANGE, "direct", true);
            ch.queueDeclare(STOCK_DLQ, true, false, false, null);
            ch.queueBind(STOCK_DLQ, DLX_EXCHANGE, "stock");

            // 声明主队列（绑定到 trade.order 交换机，带死信配置）
            ch.exchangeDeclare(ORDER_EXCHANGE, "direct", true);
            ch.queueDeclare(STOCK_QUEUE, true, false, false,
                    java.util.Map.of("x-dead-letter-exchange", DLX_EXCHANGE,
                            "x-dead-letter-routing-key", "stock"));
            ch.queueBind(STOCK_QUEUE, ORDER_EXCHANGE, "stock");

            log.info("item-service：stock.deduct 队列声明完成");
            ch.close();
        } catch (Exception e) {
            log.error("item-service MQ 初始化失败", e);
        }
    }
}
