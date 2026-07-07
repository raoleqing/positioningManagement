package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReconciliationDetailVO {
    private Long id;
    private Long reportId;
    private String orderNo;
    private String externalOrderNo;
    private LocalDateTime tradeTime;
    private BigDecimal amount;
    private String ourStatus;
    private String externalStatus;
    private String diffType;
    private String diffDesc;
    private String processStatus;
    private String processRemark;
    private LocalDateTime createTime;
}
