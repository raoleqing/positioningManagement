-- 告警配置表
CREATE TABLE IF NOT EXISTS `alert_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `alert_type` VARCHAR(50) NOT NULL COMMENT '告警类型: RECHARGE_FAIL-充值失败, BALANCE_LOW-预存款不足',
    `dingtalk_webhook_url` VARCHAR(500) DEFAULT NULL COMMENT '钉钉Webhook地址',
    `dingtalk_enabled` TINYINT DEFAULT 0 COMMENT '是否启用钉钉告警: 0-禁用, 1-启用',
    `email_recipients` VARCHAR(1000) DEFAULT NULL COMMENT '邮件接收人，多个用逗号分隔',
    `email_enabled` TINYINT DEFAULT 0 COMMENT '是否启用邮件告警: 0-禁用, 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_alert_type` (`alert_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警配置表';

-- 初始化两条配置记录
INSERT INTO `alert_config` (`alert_type`, `dingtalk_webhook_url`, `dingtalk_enabled`, `email_recipients`, `email_enabled`)
VALUES ('RECHARGE_FAIL', '', 0, '', 0)
ON DUPLICATE KEY UPDATE `alert_type` = VALUES(`alert_type`);

INSERT INTO `alert_config` (`alert_type`, `dingtalk_webhook_url`, `dingtalk_enabled`, `email_recipients`, `email_enabled`)
VALUES ('BALANCE_LOW', '', 0, '', 0)
ON DUPLICATE KEY UPDATE `alert_type` = VALUES(`alert_type`);
