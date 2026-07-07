package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReconciliationReportVO {
    private Long id;
    private String reportNo;
    private LocalDate reportDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalCount;
    private Integer matchCount;
    private Integer diffCount;
    private BigDecimal totalAmount;
    private BigDecimal diffAmount;
    private String status;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
