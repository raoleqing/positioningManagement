package com.zns.positioning.positioningmanagement.common.enums;

import lombok.Getter;

/**
 * 订单日志类型枚举
 */
@Getter
public enum OrderLogTypeEnum {
    PAY_CALLBACK("支付回调"),
    RECHARGE_CALL("调用充值"),
    RECHARGE_RESULT("充值结果"),
    NOTIFY_CUSTOMER("通知客户"),
    MANUAL_RETRY("手动重试"),
    VALIDITY_UPDATE("有效期修正"),
    OTHER("其他");

    private final String desc;

    OrderLogTypeEnum(String desc) {
        this.desc = desc;
    }
}
