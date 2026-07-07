package com.zns.positioning.positioningmanagement.dto;

import lombok.Data;

/**
 * 小程序端-充值记录查询
 */
@Data
public class MobileOrderQueryDTO {

    /** 用户ID */
    private Long userId;

    /** 设备ID */
    private Long deviceId;

    /** 支付状态 */
    private String payStatus;

    /** 页码 (默认1) */
    private Integer pageNum = 1;

    /** 每页条数 (默认10) */
    private Integer pageSize = 10;
}
