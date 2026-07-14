-- ============================================================
-- 每日流水表 — 用于流水确认/对账
-- ============================================================

DROP TABLE IF EXISTS `daily_flow`;
CREATE TABLE `daily_flow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `flow_date` DATE NOT NULL COMMENT '流水日期',
    `total_order_count` INT DEFAULT 0 COMMENT '订单总数',
    `paid_order_count` INT DEFAULT 0 COMMENT '已支付订单数',
    `paid_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '已支付总金额(元)',
    `recharge_success_count` INT DEFAULT 0 COMMENT '充值成功数',
    `recharge_success_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '充值成功金额(元)',
    `recharge_failed_count` INT DEFAULT 0 COMMENT '充值失败数',
    `recharge_failed_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '充值失败金额(元)',
    `refund_count` INT DEFAULT 0 COMMENT '退款数',
    `refund_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '退款金额(元)',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_flow_date` (`flow_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日流水表';
