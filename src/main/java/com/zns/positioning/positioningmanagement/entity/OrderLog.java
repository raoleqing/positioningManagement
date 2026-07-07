package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单操作日志
 */
@Data
@TableName("order_log")
public class OrderLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String orderNo;

    private String logType;

    private String logLevel;

    private String title;

    private String content;

    private String operator;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
