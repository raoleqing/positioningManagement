package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("reconciliation_detail")
public class ReconciliationDetail {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reportId;
    private String orderNo;
    private String externalOrderNo;
    private LocalDateTime tradeTime;
    private BigDecimal amount;
    private String ourStatus;
    private String externalStatus;
    private String diffType;
    private String diffDesc;
    private String processStatus;
    private String processRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
