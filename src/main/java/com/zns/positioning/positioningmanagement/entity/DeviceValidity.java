package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备有效期
 */
@Data
@TableName("device_validity")
public class DeviceValidity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    private String deviceNo;

    private String simCardNo;

    private LocalDate validFrom;

    private LocalDate validTo;

    private String status;

    private LocalDateTime lastRechargeTime;

    private BigDecimal totalRechargeAmount;

    private String operator;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
