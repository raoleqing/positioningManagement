package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小程序端-充值记录明细VO
 */
@Data
public class MobileOrderDetailVO {

    private Long id;
    private String orderNo;
    private Long deviceId;
    private String deviceNo;
    private String simCardNo;
    private Long planId;
    private String planName;
    private BigDecimal planAmount;
    private Integer planYears;
    private String payStatus;
    private String rechargeStatus;
    private String notifyStatus;
    private LocalDateTime payTime;
    private LocalDateTime rechargeTime;
    private LocalDateTime createTime;
    private Integer retryCount;
    private String errorMsg;
    private String remark;
}
