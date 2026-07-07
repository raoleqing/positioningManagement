package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pre_deposit_alert_config")
public class PreDepositAlertConfig {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long accountId;
    private BigDecimal alertThreshold;
    private Integer alertEnabled;
    private String alertPhone;
    private String alertEmail;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
