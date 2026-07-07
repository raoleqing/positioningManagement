package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reconciliation_report")
public class ReconciliationReport {

    @TableId(type = IdType.AUTO)
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
