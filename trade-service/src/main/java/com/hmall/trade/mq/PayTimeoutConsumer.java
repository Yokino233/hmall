package com.hmall.trade.mq;

import com.hmall.common.exception.BizIllegalException;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 支付超时消费者。
 * 监听支付超时死信队列，自动取消未支付的订单。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayTimeoutConsumer {

    private final IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "pay.timeout.dlq", durable = "true"),
            exchange = @Exchange(name = "trade.dlx", type = ExchangeTypes.DIRECT),
            key = "pay.timeout"
    ))
    public void handlePayTimeout(Long orderId) {
        log.info("支付超时，自动取消订单: {}", orderId);
        try {
            orderService.cancelOrder(orderId);
            log.info("订单 {} 已自动取消（支付超时）", orderId);
        } catch (BizIllegalException e) {
            log.info("订单 {} 取消跳过: {}", orderId, e.getMessage());
        } catch (Exception e) {
            log.error("订单 {} 自动取消失败", orderId, e);
        }
    }
}
