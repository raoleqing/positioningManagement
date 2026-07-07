package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 小程序端-设备简要信息
 */
@Data
public class DeviceSimplifyVO {

    private Long deviceId;
    private String deviceNo;
    private String simCardNo;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String status;
    private Long remainingDays;
    private BigDecimal totalRechargeAmount;
}
