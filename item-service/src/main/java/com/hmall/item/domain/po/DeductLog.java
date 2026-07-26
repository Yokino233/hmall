package com.hmall.item.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("deduct_log")
public class DeductLog {
    private Long id;
    private String txId;
    private Long itemId;
    private Integer num;
    private Integer status;
    private LocalDateTime createdAt;
}
