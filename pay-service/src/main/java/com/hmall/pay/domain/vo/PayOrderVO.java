package com.hmall.pay.domain.vo;
import io.swagger.annotations.ApiModel; import io.swagger.annotations.ApiModelProperty; import lombok.Data;
import java.time.LocalDateTime;
@Data @ApiModel(description="支付单vo实体")
public class PayOrderVO {
    private Long id; private Long bizOrderNo; private Long payOrderNo; private Long bizUserId;
    private String payChannelCode; private Integer amount; private Integer payType; private Integer status;
    private String expandJson; private String resultCode; private String resultMsg;
    private LocalDateTime paySuccessTime; private LocalDateTime payOverTime; private String qrCodeUrl;
    private LocalDateTime createTime; private LocalDateTime updateTime;
}