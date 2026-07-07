package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预存款账户
 */
@Data
@TableName("pre_deposit_account")
public class PreDepositAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String accountName;

    private BigDecimal balance;

    private BigDecimal totalDeposit;

    private BigDecimal totalConsume;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
