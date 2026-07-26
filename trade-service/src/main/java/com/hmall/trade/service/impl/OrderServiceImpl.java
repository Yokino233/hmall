package com.hmall.trade.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.utils.UserContext;
import com.hmall.trade.config.MqConfig;
import com.hmall.trade.domain.dto.OrderFormDTO;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.domain.po.OrderDetail;
import com.hmall.trade.mapper.OrderMapper;
import com.hmall.trade.service.IOrderDetailService;
import com.hmall.trade.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper,Order> implements IOrderService {
    private final ItemClient itemClient;
    private final IOrderDetailService detailService;
    private final RabbitTemplate rabbitTemplate;
    @Override @GlobalTransactional @Transactional
    public Long createOrder(OrderFormDTO orderFormDTO){
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
        Map<Long,Integer> itemNumMap = detailDTOS.stream().collect(Collectors.toMap(OrderDetailDTO::getItemId,OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        List<ItemDTO> items = itemClient.queryItemsByIds(itemIds);
        if(items==null||items.size()<itemIds.size())throw new BadRequestException("商品不存在");
        int total=0; for(ItemDTO item:items)total+=item.getPrice()*itemNumMap.get(item.getId());
        Order order=new Order(); order.setTotalFee(total); order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser()); order.setStatus(1); save(order);
        List<OrderDetail> details=buildDetails(order.getId(),items,itemNumMap);
        detailService.saveBatch(details);

        // 发 MQ 消息：异步扣库存、清购物车
        String msgId = order.getId() + "-" + System.currentTimeMillis();
        rabbitTemplate.convertAndSend(MqConfig.ORDER_EXCHANGE, "stock", detailDTOS, m -> {
            m.getMessageProperties().setMessageId(msgId);
            return m;
        });
        rabbitTemplate.convertAndSend(MqConfig.ORDER_EXCHANGE, "cart", order.getUserId());

        return order.getId();
    }
    @Override
    public void markOrderPaySuccess(Long orderId){
        Order order=new Order(); order.setId(orderId); order.setStatus(2); order.setPayTime(LocalDateTime.now()); updateById(order);
    }
    private List<OrderDetail> buildDetails(Long orderId,List<ItemDTO> items,Map<Long,Integer> numMap){
        List<OrderDetail> details=new ArrayList<>(items.size());
        for(ItemDTO item:items){
            OrderDetail d=new OrderDetail(); d.setName(item.getName()); d.setSpec(item.getSpec());
            d.setPrice(item.getPrice()); d.setNum(numMap.get(item.getId())); d.setItemId(item.getId());
            d.setImage(item.getImage()); d.setOrderId(orderId); details.add(d);
        }
        return details;
    }
}