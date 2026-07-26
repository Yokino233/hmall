package com.hmall.item.mq;

import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.domain.po.DeductLog;
import com.hmall.item.mapper.DeductLogMapper;
import com.hmall.item.service.IItemService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockDeductConsumer {

    private final IItemService itemService;
    private final DeductLogMapper deductLogMapper;

    @RabbitListener(queues = "stock.deduct")
    public void handleStockDeduct(List<OrderDetailDTO> items, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // txId 用消息 ID 保证幂等
        String msgId = message.getMessageProperties().getMessageId();
        final String txId = msgId != null ? msgId : UUID.randomUUID().toString();

        try {
            // 幂等检查：是否已处理过
            long count = deductLogMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeductLog>()
                            .eq(DeductLog::getTxId, txId));
            if (count > 0) {
                log.info("消息已处理过，跳过: txId={}", txId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 扣库存
            List<com.hmall.item.domain.dto.OrderDetailDTO> itemDetails =
                    BeanUtils.copyList(items, com.hmall.item.domain.dto.OrderDetailDTO.class);
            itemService.deductStock(itemDetails);

            // 记录已处理
            List<DeductLog> logs = items.stream().map(i -> {
                DeductLog log = new DeductLog();
                log.setTxId(txId);
                log.setItemId(i.getItemId());
                log.setNum(i.getNum());
                log.setStatus(1);
                return log;
            }).collect(Collectors.toList());
            logs.forEach(deductLogMapper::insert);

            // 手动 ACK
            channel.basicAck(deliveryTag, false);
            log.info("扣库存成功 + ACK: txId={}", txId);
        } catch (Exception e) {
            log.error("扣库存失败，将进入死信队列: txId={}", txId, e);
            try {
                // NACK，不重新入队（进死信）
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("NACK 失败", ex);
            }
        }
    }
}
