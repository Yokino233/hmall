package com.hmall.trade.controller;
import com.hmall.common.utils.BeanUtils;
import com.hmall.trade.domain.dto.OrderFormDTO;
import com.hmall.trade.domain.vo.OrderVO;
import com.hmall.trade.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@Api(tags="订单管理接口") @RestController @RequestMapping("/orders") @RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    @GetMapping("{id}")
    public OrderVO queryOrderById(@PathVariable("id")Long orderId){return BeanUtils.copyBean(orderService.getById(orderId),OrderVO.class);}
    @PostMapping
    public Long createOrder(@RequestBody OrderFormDTO orderFormDTO){return orderService.createOrder(orderFormDTO);}
    @PutMapping("/{orderId}")
    public void markOrderPaySuccess(@PathVariable("orderId")Long orderId){orderService.markOrderPaySuccess(orderId);}
    @ApiOperation("取消订单（仅未支付可取消）")
    @PutMapping("/{orderId}/cancel")
    public void cancelOrder(@PathVariable("orderId") Long orderId){orderService.cancelOrder(orderId);}
}