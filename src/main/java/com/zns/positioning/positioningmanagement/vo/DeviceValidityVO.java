package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DeviceValidityVO {
    private Long id;
    private Long deviceId;
    private String deviceNo;
    private String simCardNo;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String status;
    private LocalDateTime lastRechargeTime;
    private BigDecimal totalRechargeAmount;
    private Long remainingDays;
}
