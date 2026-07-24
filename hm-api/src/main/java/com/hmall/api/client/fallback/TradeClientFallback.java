package com.hmall.api.client.fallback;

import com.hmall.api.client.TradeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TradeClientFallback implements TradeClient {
    @Override
    public void markOrderPaySuccess(Long orderId) {
        log.error("trade-service 订单支付回调失败，触发降级，orderId={}", orderId);
        throw new RuntimeException("订单服务不可用，请稍后重试");
    }
}
