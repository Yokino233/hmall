package com.hmall.cart.mq;

import com.hmall.cart.service.ICartService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartCleanConsumer {

    private final ICartService cartService;

    @RabbitListener(queues = "cart.clean")
    public void handleCartClean(Long userId, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            cartService.cleanCartByUserId(userId);
            channel.basicAck(deliveryTag, false);
            log.info("清空购物车成功 + ACK: userId={}", userId);
        } catch (Exception e) {
            log.error("清空购物车失败，将重试: userId={}", userId, e);
            try {
                // requeue=true 重新入队重试
                channel.basicNack(deliveryTag, false, true);
            } catch (Exception ex) {
                log.error("NACK 失败", ex);
            }
        }
    }
}
