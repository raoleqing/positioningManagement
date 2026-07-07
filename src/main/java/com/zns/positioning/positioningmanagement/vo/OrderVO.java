package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String userName;
    private Long deviceId;
    private String deviceNo;
    private String simCardNo;
    private BigDecimal amount;
    private Integer years;
    private String payStatus;
    private String rechargeStatus;
    private String notifyStatus;
    private String wxTransactionId;
    private LocalDateTime payTime;
    private LocalDateTime rechargeTime;
    private LocalDateTime notifyTime;
    private Integer retryCount;
    private String errorMsg;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
