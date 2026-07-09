package com.zns.positioning.positioningmanagement.common.enums;

import lombok.Getter;

/**
 * 通用操作日志类型枚举
 * logType 存储 enum name，businessType 用于区分业务模块
 */
@Getter
public enum OperationLogTypeEnum {

    // ========== 套餐管理 ==========
    PACKAGE_CREATE("PACKAGE_PLAN", "新增套餐"),
    PACKAGE_UPDATE("PACKAGE_PLAN", "修改套餐"),
    PACKAGE_DELETE("PACKAGE_PLAN", "删除套餐"),
    PACKAGE_STATUS_CHANGE("PACKAGE_PLAN", "状态变更"),

    // ========== 订单 ==========
    ORDER_CREATE("ORDER", "创建订单"),
    ORDER_PAY_CALLBACK("ORDER", "支付回调"),
    ORDER_RECHARGE_CALL("ORDER", "调用充值"),
    ORDER_RECHARGE_RESULT("ORDER", "充值结果"),
    ORDER_NOTIFY_CUSTOMER("ORDER", "通知客户"),
    ORDER_MANUAL_RETRY("ORDER", "手动重试"),
    ORDER_OTHER("ORDER", "其他");

    /** 业务类型: PACKAGE_PLAN / ORDER */
    private final String businessType;
    /** 操作描述 */
    private final String desc;

    OperationLogTypeEnum(String businessType, String desc) {
        this.businessType = businessType;
        this.desc = desc;
    }
}
