package com.hmall.pay.domain.dto;
import io.swagger.annotations.ApiModel; import io.swagger.annotations.ApiModelProperty; import lombok.Builder; import lombok.Data; import lombok.NoArgsConstructor; import lombok.AllArgsConstructor;
import javax.validation.constraints.NotNull;
@Data @Builder @ApiModel(description="支付确认表单实体")
@NoArgsConstructor @AllArgsConstructor
public class PayOrderFormDTO {
    @NotNull private Long id; @NotNull private String pw;
}
