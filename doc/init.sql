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
    `plan_id` BIGINT DEFAULT NULL COMMENT '套餐ID',
    `plan_name` VARCHAR(100) DEFAULT NULL COMMENT '套餐名称（冗余存储，快照）',
    `plan_amount` DECIMAL(10,2) NOT NULL COMMENT '套餐金额(元)',
    `plan_years` INT NOT NULL DEFAULT 1 COMMENT '套餐年数',
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
    KEY `idx_deleted` (`deleted`),
    KEY `idx_plan_id` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值订单表';

-- 通用操作日志表（统一记录套餐、订单等所有业务的操作日志）
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `business_type` VARCHAR(30) NOT NULL COMMENT '业务类型: PACKAGE_PLAN-套餐, ORDER-订单',
    `business_id` BIGINT DEFAULT NULL COMMENT '业务主键ID（套餐ID/订单ID/设备ID）',
    `business_no` VARCHAR(64) DEFAULT NULL COMMENT '业务编号（订单号/设备编号等）',
    `business_name` VARCHAR(100) DEFAULT NULL COMMENT '业务名称快照（套餐名/设备编号等）',
    `log_type` VARCHAR(50) NOT NULL COMMENT '日志类型（如: PACKAGE_CREATE, ORDER_PAY_CALLBACK 等）',
    `log_level` VARCHAR(10) NOT NULL DEFAULT 'INFO' COMMENT '日志级别: INFO, WARN, ERROR',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '日志标题',
    `content` TEXT COMMENT '日志内容(JSON格式，含各业务特有字段)',
    `operator` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_business_type_id` (`business_type`, `business_id`),
    KEY `idx_business_no` (`business_no`),
    KEY `idx_log_type` (`log_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用操作日志表';

-- 套餐管理表
CREATE TABLE IF NOT EXISTS `package_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `plan_name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
    `years` INT NOT NULL COMMENT '年数',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格(元)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序(数字越小越靠前)',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐管理表';

-- 注: 套餐操作日志已合并到通用 operation_log 表中(business_type='PACKAGE_PLAN')

-- ============================================
-- 初始化默认数据
-- ============================================


