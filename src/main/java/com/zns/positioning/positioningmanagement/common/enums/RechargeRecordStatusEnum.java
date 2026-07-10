package com.zns.positioning.positioningmanagement.common.enums;

import lombok.Getter;

/**
 * 充值记录状态枚举
 */
@Getter
public enum RechargeRecordStatusEnum {

    SUCCESS("扣费成功"),
    FAILED("扣费失败"),
    CALL_FAILED("接口调用异常");

    private final String desc;

    RechargeRecordStatusEnum(String desc) {
        this.desc = desc;
    }
}
