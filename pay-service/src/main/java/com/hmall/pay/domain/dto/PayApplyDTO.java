package com.hmall.pay.domain.dto;
import io.swagger.annotations.ApiModel; import io.swagger.annotations.ApiModelProperty; import lombok.Builder; import lombok.Data;
import javax.validation.constraints.Min; import javax.validation.constraints.NotNull;
@Data @Builder @ApiModel(description="支付下单表单实体")
public class PayApplyDTO {
    @NotNull private Long bizOrderNo; @Min(1) private Integer amount;
    @NotNull private String payChannelCode; @NotNull private Integer payType;
    @NotNull private String orderInfo;
}