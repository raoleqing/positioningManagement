package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PreDepositAccountVO {
    private Long id;
    private String accountName;
    private BigDecimal balance;
    private BigDecimal totalDeposit;
    private BigDecimal totalConsume;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
