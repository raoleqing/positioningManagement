package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PreDepositRecordVO {
    private Long id;
    private Long accountId;
    private Long orderId;
    private String orderNo;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String remark;
    private LocalDateTime createTime;
}
