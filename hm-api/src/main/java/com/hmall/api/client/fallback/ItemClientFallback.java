package com.hmall.api.client.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ItemClientFallback implements ItemClient {
    @Override
    public List<ItemDTO> queryItemsByIds(Collection<Long> ids) {
        log.error("item-service 调用失败，触发降级，ids={}", ids);
        return Collections.emptyList();
    }
}
