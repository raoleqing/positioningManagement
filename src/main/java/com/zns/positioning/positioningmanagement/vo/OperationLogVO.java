package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通用操作日志 VO
 */
@Data
public class OperationLogVO {

    private Long id;

    /** 业务类型: PACKAGE_PLAN、ORDER */
    private String businessType;

    /** 业务主键ID */
    private Long businessId;

    /** 业务编号 */
    private String businessNo;

    /** 业务名称快照 */
    private String businessName;

    /** 日志类型 */
    private String logType;

    /** 日志类型中文描述 */
    private String logTypeText;

    /** 日志级别 */
    private String logLevel;

    /** 日志标题 */
    private String title;

    /** 日志内容(JSON格式) */
    private String content;

    /** 操作人 */
    private String operator;

    /** 创建时间 */
    private LocalDateTime createTime;
}
