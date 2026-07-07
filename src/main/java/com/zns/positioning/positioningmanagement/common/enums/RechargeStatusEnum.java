package com.zns.positioning.positioningmanagement.common.enums;

import lombok.Getter;

/**
 * 充值状态枚举
 */
@Getter
public enum RechargeStatusEnum {
    PENDING("待充值"),
    SUCCESS("充值成功"),
    FAILED("充值失败"),
    RETRYING("重试中");

    private final String desc;

    RechargeStatusEnum(String desc) {
        this.desc = desc;
    }
}
