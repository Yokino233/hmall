package com.hmall.api.client.fallback;

import com.hmall.api.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallback implements UserClient {
    @Override
    public void deductMoney(String pw, Integer amount) {
        log.error("user-service 扣款调用失败，触发降级，amount={}", amount);
        throw new RuntimeException("扣款服务不可用，请稍后重试");
    }
}
