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

    /** 套餐ID */
    private Long planId;

    /** 套餐名称（冗余存储，创建订单时快照） */
    private String planName;

    /** 套餐金额(元) */
    private BigDecimal planAmount;

    /** 套餐年数 */
    private Integer planYears;

    private String payStatus;

    private String rechargeStatus;

    private String notifyStatus;

    private String wxTransactionId;

    private LocalDateTime payTime;

    private LocalDateTime rechargeTime;

    private LocalDateTime notifyTime;

    /** 重试次数 */
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
