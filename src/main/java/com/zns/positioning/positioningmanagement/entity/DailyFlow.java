package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日流水表 — 每日充值业务的汇总数据，用于流水确认/对账
 */
@Data
@TableName("daily_flow")
public class DailyFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流水日期 */
    private LocalDate flowDate;

    /** 订单总数 */
    private Integer totalOrderCount;

    /** 已支付订单数 */
    private Integer paidOrderCount;

    /** 已支付总金额(元) */
    private BigDecimal paidAmount;

    /** 充值成功数（运营商扣费成功） */
    private Integer rechargeSuccessCount;

    /** 充值成功金额(元) */
    private BigDecimal rechargeSuccessAmount;

    /** 充值失败数 */
    private Integer rechargeFailedCount;

    /** 充值失败金额(元) */
    private BigDecimal rechargeFailedAmount;

    /** 退款数 */
    private Integer refundCount;

    /** 退款金额(元) */
    private BigDecimal refundAmount;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
