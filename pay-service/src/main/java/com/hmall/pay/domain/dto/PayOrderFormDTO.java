package com.hmall.pay.domain.dto;
import io.swagger.annotations.ApiModel; import io.swagger.annotations.ApiModelProperty; import lombok.Builder; import lombok.Data;
import javax.validation.constraints.NotNull;
@Data @Builder @ApiModel(description="支付确认表单实体")
public class PayOrderFormDTO {
    @NotNull private Long id; @NotNull private String pw;
}