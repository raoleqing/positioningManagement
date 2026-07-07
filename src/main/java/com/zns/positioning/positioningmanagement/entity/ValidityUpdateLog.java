package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备有效期修正日志
 */
@Data
@TableName("validity_update_log")
public class ValidityUpdateLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    private String deviceNo;

    private LocalDate oldValidFrom;

    private LocalDate oldValidTo;

    private LocalDate newValidFrom;

    private LocalDate newValidTo;

    private String reason;

    private String operator;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
