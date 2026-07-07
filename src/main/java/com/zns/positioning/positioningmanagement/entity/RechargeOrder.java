package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值订单
 */
@Data
@TableName("recharge_order")
public class RechargeOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private String userName;

    private Long deviceId;

    private String deviceNo;

    private String simCardNo;

    private BigDecimal amount;

    private Integer years;

    private String payStatus;

    private String rechargeStatus;

    private String notifyStatus;

    private String wxTransactionId;

    private LocalDateTime payTime;

    private LocalDateTime rechargeTime;

    private LocalDateTime notifyTime;

    private Integer retryCount;

    private Integer maxRetryCount;

    private String errorMsg;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
