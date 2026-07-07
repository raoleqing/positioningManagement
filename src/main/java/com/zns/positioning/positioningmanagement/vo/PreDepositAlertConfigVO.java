package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PreDepositAlertConfigVO {
    private Long id;
    private Long accountId;
    private BigDecimal alertThreshold;
    private Integer alertEnabled;
    private String alertPhone;
    private String alertEmail;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
