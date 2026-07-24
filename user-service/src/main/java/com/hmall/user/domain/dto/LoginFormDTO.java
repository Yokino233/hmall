package com.hmall.user.domain.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
@Data @ApiModel(description="登录表单实体")
public class LoginFormDTO {
    @NotNull @ApiModelProperty(value="用户名",required=true)
    private String username;
    @NotNull @ApiModelProperty(value="密码",required=true)
    private String password;
    @ApiModelProperty(value="是否记住我")
    private Boolean rememberMe = false;
}
