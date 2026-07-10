package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值记录表 — 记录每一笔订单调用运营商扣费的明细
 */
@Data
@TableName("recharge_record")
public class RechargeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 设备ID */
    private Long deviceId;

    /** 设备编号 */
    private String deviceNo;

    /** SIM卡ICCID */
    private String iccid;

    /** 运营商套餐ID */
    private Long packageId;

    /** 套餐名称 */
    private String planName;

    /** 扣费金额(元) */
    private BigDecimal amount;

    /** 充值状态: SUCCESS-扣费成功, FAILED-扣费失败, CALL_FAILED-接口调用异常 */
    private String rechargeStatus;

    /** 运营商API返回的code */
    private String respCode;

    /** 运营商API返回的msg */
    private String respMsg;

    /** 运营商API返回的完整响应体(JSON) */
    private String responseBody;

    /** 失败原因/错误信息 */
    private String errorMsg;

    /** 扣费时间 */
    private LocalDateTime rechargeTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除: 0-未删除, 1-已删除 */
    @TableLogic
    private Integer deleted;
}
