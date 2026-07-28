package com.hmall.trade.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
@EnableRabbit
public class MqConfig {

    public static final String ORDER_EXCHANGE = "trade.order";
    public static final String STOCK_QUEUE = "stock.deduct";
    public static final String CART_QUEUE = "cart.clean";
    public static final String DLX_EXCHANGE = "trade.dlx";
    public static final String STOCK_DLQ = "stock.deduct.dlq";

    /** 支付超时延迟队列（TTL 30分钟） */
    public static final String PAY_TIMEOUT_QUEUE = "pay.timeout";
    public static final String PAY_TIMEOUT_DLQ = "pay.timeout.dlq";

    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactory connectionFactory;

    public MqConfig(RabbitTemplate rabbitTemplate, ConnectionFactory connectionFactory) {
        this.rabbitTemplate = rabbitTemplate;
        this.connectionFactory = connectionFactory;
    }

    @PostConstruct
    public void init() {
        // 发送者确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("MQ 确认收到消息: {}", correlationData);
            } else {
                log.error("MQ 确认失败: {}, cause: {}", correlationData, cause);
            }
        });

        // 创建交换机、队列、死信队列
        try {
            Connection conn = (Connection) connectionFactory.createConnection().getDelegate();
            Channel ch = conn.createChannel();

            ch.exchangeDeclare(DLX_EXCHANGE, "direct", true);
            ch.queueDeclare(STOCK_DLQ, true, false, false, null);
            ch.queueBind(STOCK_DLQ, DLX_EXCHANGE, "stock");

            ch.exchangeDeclare(ORDER_EXCHANGE, "direct", true);
            ch.queueDeclare(STOCK_QUEUE, true, false, false,
                    java.util.Map.of("x-dead-letter-exchange", DLX_EXCHANGE,
                            "x-dead-letter-routing-key", "stock"));
            ch.queueDeclare(CART_QUEUE, true, false, false, null);
            ch.queueBind(STOCK_QUEUE, ORDER_EXCHANGE, "stock");
            ch.queueBind(CART_QUEUE, ORDER_EXCHANGE, "cart");

            // 支付超时队列（TTL 30分钟 → DLQ）
            ch.queueDeclare(PAY_TIMEOUT_DLQ, true, false, false, null);
            ch.queueBind(PAY_TIMEOUT_DLQ, DLX_EXCHANGE, "pay.timeout");
            ch.queueDeclare(PAY_TIMEOUT_QUEUE, true, false, false,
                    java.util.Map.of("x-dead-letter-exchange", DLX_EXCHANGE,
                            "x-dead-letter-routing-key", "pay.timeout",
                            "x-message-ttl", 30 * 60 * 1000)); // 30分钟
            ch.queueBind(PAY_TIMEOUT_QUEUE, ORDER_EXCHANGE, "pay.timeout");

            log.info("RabbitMQ 队列/交换机/死信队列已创建");
            ch.close();
        } catch (Exception e) {
            log.error("RabbitMQ 初始化失败", e);
        }
    }
}
