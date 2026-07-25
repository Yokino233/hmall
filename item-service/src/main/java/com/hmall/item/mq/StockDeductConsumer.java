package com.hmall.item.mq;

import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.service.IItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDeductConsumer {

    private final IItemService itemService;

    @RabbitListener(queues = "stock.deduct")
    public void handleStockDeduct(List<OrderDetailDTO> items) {
        log.info("收到扣库存消息: {}", items);
        try {
            List<com.hmall.item.domain.dto.OrderDetailDTO> itemDetails =
                    BeanUtils.copyList(items, com.hmall.item.domain.dto.OrderDetailDTO.class);
            itemService.deductStock(itemDetails);
            log.info("扣库存成功");
        } catch (Exception e) {
            log.error("扣库存失败", e);
        }
    }
}
