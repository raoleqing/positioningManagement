package com.zns.positioning.positioningmanagement.common.enums;

import lombok.Getter;

/**
 * 通知状态枚举
 */
@Getter
public enum NotifyStatusEnum {
    PENDING("待通知"),
    SUCCESS("通知成功"),
    FAILED("通知失败");

    private final String desc;

    NotifyStatusEnum(String desc) {
        this.desc = desc;
    }
}
