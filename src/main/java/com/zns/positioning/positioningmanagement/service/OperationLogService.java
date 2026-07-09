package com.zns.positioning.positioningmanagement.service;

import com.zns.positioning.positioningmanagement.common.enums.OperationLogTypeEnum;
import com.zns.positioning.positioningmanagement.vo.OperationLogVO;

import java.util.List;

/**
 * 通用操作日志 Service
 */
public interface OperationLogService {

    /**
     * 保存操作日志
     *
     * @param logType      日志类型枚举
     * @param businessId   业务主键ID
     * @param businessNo   业务编号
     * @param businessName 业务名称快照
     * @param logLevel     日志级别: INFO / WARN / ERROR
     * @param title        日志标题
     * @param content      日志内容
     * @param operator     操作人
     */
    void saveLog(OperationLogTypeEnum logType, Long businessId, String businessNo,
                 String businessName, String logLevel, String title,
                 String content, String operator);

    /**
     * 按业务类型和业务ID查询日志列表
     *
     * @param businessType 业务类型: PACKAGE_PLAN / ORDER
     * @param businessId   业务主键ID
     * @return 日志列表（按时间倒序）
     */
    List<OperationLogVO> getLogs(String businessType, Long businessId);
}
