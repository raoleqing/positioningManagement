package com.zns.positioning.positioningmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.common.enums.OperationLogTypeEnum;
import com.zns.positioning.positioningmanagement.vo.OperationLogVO;

import java.time.LocalDateTime;
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

    /**
     * 分页查询操作日志（支持多条件筛选）
     *
     * @param page         分页参数
     * @param businessNo   业务编号（订单号等），模糊搜索
     * @param businessType 业务类型，可选
     * @param logLevel     日志级别，可选
     * @param startTime    开始时间，可选
     * @param endTime      结束时间，可选
     * @return 分页结果
     */
    Page<OperationLogVO> queryPage(Page<OperationLogVO> page, String businessNo,
                                   String businessType, String logLevel,
                                   LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据业务编号（订单号）查询完整操作链路
     *
     * @param businessNo 业务编号（订单号）
     * @return 日志列表（按时间升序，展示全链路）
     */
    List<OperationLogVO> queryByBusinessNo(String businessNo);
}
