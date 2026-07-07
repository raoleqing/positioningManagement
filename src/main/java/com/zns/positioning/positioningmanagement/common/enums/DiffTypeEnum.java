package com.zns.positioning.positioningmanagement.common.enums;

import lombok.Getter;

/**
 * 对账差异类型枚举
 */
@Getter
public enum DiffTypeEnum {
    NO_DIFF("无差异"),
    AMOUNT_DIFF("金额差异"),
    MISSING_OUR("我方缺失"),
    MISSING_EXTERNAL("运营商缺失"),
    STATUS_DIFF("状态差异");

    private final String desc;

    DiffTypeEnum(String desc) {
        this.desc = desc;
    }
}
