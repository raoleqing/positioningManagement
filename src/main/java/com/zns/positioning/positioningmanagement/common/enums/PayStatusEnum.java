package com.zns.positioning.positioningmanagement.common.enums;

import lombok.Getter;

/**
 * 订单支付状态枚举
 */
@Getter
public enum PayStatusEnum {
    PENDING("待支付"),
    PAID("已支付"),
    PAY_FAILED("支付失败"),
    REFUNDED("已退款");

    private final String desc;

    PayStatusEnum(String desc) {
        this.desc = desc;
    }
}
