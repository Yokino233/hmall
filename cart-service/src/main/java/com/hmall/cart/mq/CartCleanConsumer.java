package com.hmall.cart.mq;

import com.hmall.cart.service.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartCleanConsumer {

    private final ICartService cartService;

    @RabbitListener(queues = "cart.clean")
    public void handleCartClean(Long userId) {
        log.info("收到清空购物车消息: userId={}", userId);
        try {
            cartService.cleanCartByUserId(userId);
            log.info("清空购物车成功");
        } catch (Exception e) {
            log.error("清空购物车失败", e);
        }
    }
}
