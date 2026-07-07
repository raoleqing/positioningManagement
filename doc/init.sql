-- ============================================
-- 定位器年费管理平台 - 数据库初始化脚本
-- ============================================

-- 充值订单表
CREATE TABLE IF NOT EXISTS `recharge_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(64) DEFAULT NULL COMMENT '用户昵称',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_no` VARCHAR(64) DEFAULT NULL COMMENT '设备编号',
    `sim_card_no` VARCHAR(32) DEFAULT NULL COMMENT 'SIM卡号',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '充值金额(元)',
    `years` INT NOT NULL DEFAULT 1 COMMENT '充值年数',
    `pay_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '支付状态: PENDING-待支付, PAID-已支付, PAY_FAILED-支付失败, REFUNDED-已退款',
    `recharge_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '充值状态: PENDING-待充值, SUCCESS-充值成功, FAILED-充值失败, RETRYING-重试中',
    `notify_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '通知状态: PENDING-待通知, SUCCESS-通知成功, FAILED-通知失败',
    `wx_transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '微信支付交易号',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `recharge_time` DATETIME DEFAULT NULL COMMENT '充值完成时间',
    `notify_time` DATETIME DEFAULT NULL COMMENT '通知客户系统时间',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` INT NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_pay_status` (`pay_status`),
    KEY `idx_recharge_status` (`recharge_status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值订单表';

-- 订单操作日志表
CREATE TABLE IF NOT EXISTS `order_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '关联订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `log_type` VARCHAR(30) NOT NULL COMMENT '日志类型: PAY_CALLBACK-支付回调, RECHARGE_CALL-调用充值, RECHARGE_RESULT-充值结果, NOTIFY_CUSTOMER-通知客户, MANUAL_RETRY-手动重试, VALIDITY_UPDATE-有效期修正, OTHER-其他',
    `log_level` VARCHAR(10) NOT NULL DEFAULT 'INFO' COMMENT '日志级别: INFO, WARN, ERROR',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '日志标题',
    `content` TEXT COMMENT '日志内容(JSON格式)',
    `operator` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_log_type` (`log_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单操作日志表';

-- 设备有效期表
CREATE TABLE IF NOT EXISTS `device_validity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_no` VARCHAR(64) DEFAULT NULL COMMENT '设备编号',
    `sim_card_no` VARCHAR(32) DEFAULT NULL COMMENT 'SIM卡号',
    `valid_from` DATE DEFAULT NULL COMMENT '有效期起始日期',
    `valid_to` DATE DEFAULT NULL COMMENT '有效期截止日期',
    `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态: NORMAL-正常, EXPIRING-即将到期, EXPIRED-已到期',
    `last_recharge_time` DATETIME DEFAULT NULL COMMENT '最近一次充值时间',
    `total_recharge_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计充值金额',
    `operator` VARCHAR(64) DEFAULT NULL COMMENT '最近操作人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_id` (`device_id`),
    KEY `idx_status` (`status`),
    KEY `idx_valid_to` (`valid_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备有效期表';

-- 设备有效期修正日志表
CREATE TABLE IF NOT EXISTS `validity_update_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_no` VARCHAR(64) DEFAULT NULL COMMENT '设备编号',
    `old_valid_from` DATE DEFAULT NULL COMMENT '原有效期起始',
    `old_valid_to` DATE DEFAULT NULL COMMENT '原有效期截止',
    `new_valid_from` DATE DEFAULT NULL COMMENT '新有效期起始',
    `new_valid_to` DATE DEFAULT NULL COMMENT '新有效期截止',
    `reason` VARCHAR(255) NOT NULL COMMENT '修正原因',
    `operator` VARCHAR(64) NOT NULL COMMENT '操作人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备有效期修正日志表';

-- 预存款账户表
CREATE TABLE IF NOT EXISTS `pre_deposit_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `account_name` VARCHAR(64) NOT NULL COMMENT '账户名称',
    `balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '当前余额(元)',
    `total_deposit` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计充值金额',
    `total_consume` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计消耗金额',
    `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态: NORMAL-正常, LOW_BALANCE-低余额, FROZEN-已冻结',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预存款账户表';

-- 预存款消耗明细表
CREATE TABLE IF NOT EXISTS `pre_deposit_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `account_id` BIGINT NOT NULL COMMENT '关联账户ID',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
    `order_no` VARCHAR(32) DEFAULT NULL COMMENT '订单号',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: TOP_UP-充值, CONSUME-消耗, REFUND-退款',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '金额(元)',
    `balance_before` DECIMAL(12,2) NOT NULL COMMENT '操作前余额',
    `balance_after` DECIMAL(12,2) NOT NULL COMMENT '操作后余额',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_account_id` (`account_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_type` (`type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预存款消耗明细表';

-- 低余额告警配置表
CREATE TABLE IF NOT EXISTS `pre_deposit_alert_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `account_id` BIGINT NOT NULL COMMENT '关联账户ID',
    `alert_threshold` DECIMAL(10,2) NOT NULL DEFAULT 500.00 COMMENT '告警阈值(元)',
    `alert_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
    `alert_phone` VARCHAR(32) DEFAULT NULL COMMENT '告警通知手机号',
    `alert_email` VARCHAR(64) DEFAULT NULL COMMENT '告警通知邮箱',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_account_id` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低余额告警配置表';

-- 对账报告表
CREATE TABLE IF NOT EXISTS `reconciliation_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `report_no` VARCHAR(32) NOT NULL COMMENT '报告编号',
    `report_date` DATE NOT NULL COMMENT '对账日期',
    `start_time` DATETIME NOT NULL COMMENT '对账起始时间',
    `end_time` DATETIME NOT NULL COMMENT '对账截止时间',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '总交易笔数',
    `match_count` INT NOT NULL DEFAULT 0 COMMENT '匹配笔数',
    `diff_count` INT NOT NULL DEFAULT 0 COMMENT '差异笔数',
    `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    `diff_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '差异金额',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待处理, PROCESSING-处理中, RESOLVED-已处理, CLOSED-已关闭',
    `operator` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_report_no` (`report_no`),
    KEY `idx_report_date` (`report_date`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账报告表';

-- 对账明细表
CREATE TABLE IF NOT EXISTS `reconciliation_detail` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `report_id` BIGINT NOT NULL COMMENT '关联报告ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '我方订单号',
    `external_order_no` VARCHAR(64) DEFAULT NULL COMMENT '运营商订单号',
    `trade_time` DATETIME DEFAULT NULL COMMENT '交易时间',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '交易金额',
    `our_status` VARCHAR(20) DEFAULT NULL COMMENT '我方状态',
    `external_status` VARCHAR(20) DEFAULT NULL COMMENT '运营商状态',
    `diff_type` VARCHAR(20) DEFAULT NULL COMMENT '差异类型: NO_DIFF-无差异, AMOUNT_DIFF-金额差异, MISSING_OUR-我方缺失, MISSING_EXTERNAL-运营商缺失, STATUS_DIFF-状态差异',
    `diff_desc` VARCHAR(255) DEFAULT NULL COMMENT '差异描述',
    `process_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态: PENDING-待处理, RESOLVED-已处理, IGNORED-已忽略',
    `process_remark` VARCHAR(255) DEFAULT NULL COMMENT '处理备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_report_id` (`report_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_diff_type` (`diff_type`),
    KEY `idx_process_status` (`process_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账明细表';

-- ============================================
-- 初始化默认数据
-- ============================================
INSERT IGNORE INTO `pre_deposit_account` (`id`, `account_name`, `balance`, `total_deposit`, `total_consume`, `status`) VALUES
(1, '主账户', 10000.00, 10000.00, 0.00, 'NORMAL');

INSERT IGNORE INTO `pre_deposit_alert_config` (`id`, `account_id`, `alert_threshold`, `alert_enabled`) VALUES
(1, 1, 500.00, 1);
