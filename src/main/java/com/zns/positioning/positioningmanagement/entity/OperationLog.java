package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通用操作日志实体
 * 统一记录套餐、订单等所有业务模块的操作日志
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务类型: PACKAGE_PLAN、ORDER */
    private String businessType;

    /** 业务主键ID（planId / orderId / deviceId） */
    private Long businessId;

    /** 业务编号（orderNo / deviceNo 等） */
    private String businessNo;

    /** 业务名称快照（套餐名 / 设备编号 等） */
    private String businessName;

    /** 日志类型 */
    private String logType;

    /** 日志级别: INFO, WARN, ERROR */
    private String logLevel;

    /** 日志标题 */
    private String title;

    /** 日志内容(JSON格式，含各业务特有字段) */
    private String content;

    /** 操作人 */
    private String operator;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
