package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警配置表
 */
@Data
@TableName("alert_config")
public class AlertConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 告警类型: RECHARGE_FAIL-充值失败, BALANCE_LOW-预存款不足 */
    private String alertType;

    /** 钉钉Webhook地址 */
    private String dingtalkWebhookUrl;

    /** 是否启用钉钉告警: 0-禁用, 1-启用 */
    private Integer dingtalkEnabled;

    /** 邮件接收人（多个用逗号分隔） */
    private String emailRecipients;

    /** 是否启用邮件告警: 0-禁用, 1-启用 */
    private Integer emailEnabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
