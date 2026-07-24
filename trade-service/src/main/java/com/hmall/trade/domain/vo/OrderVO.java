package com.hmall.trade.domain.vo;
import io.swagger.annotations.ApiModel; import io.swagger.annotations.ApiModelProperty; import lombok.Data;
import java.time.LocalDateTime;
@Data @ApiModel(description="订单页面VO")
public class OrderVO {
    private Long id; private Integer totalFee; private Integer paymentType; private Long userId;
    private Integer status; private LocalDateTime createTime; private LocalDateTime payTime;
    private LocalDateTime consignTime; private LocalDateTime endTime; private LocalDateTime closeTime;
    private LocalDateTime commentTime;
}