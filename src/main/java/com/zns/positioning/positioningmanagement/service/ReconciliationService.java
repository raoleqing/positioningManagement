package com.zns.positioning.positioningmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.dto.ReconciliationDetailQueryDTO;
import com.zns.positioning.positioningmanagement.dto.ReconciliationGenerateDTO;
import com.zns.positioning.positioningmanagement.dto.ReconciliationQueryDTO;
import com.zns.positioning.positioningmanagement.vo.ReconciliationDetailVO;
import com.zns.positioning.positioningmanagement.vo.ReconciliationReportVO;

import java.time.LocalDate;

public interface ReconciliationService {

    /** 生成对账报告 */
    ReconciliationReportVO generateReport(ReconciliationGenerateDTO dto, String operator);

    /** 分页查询对账报告 */
    Page<ReconciliationReportVO> queryReports(ReconciliationQueryDTO dto);

    /** 查看对账详细差异明细 */
    Page<ReconciliationDetailVO> queryReportDetails(Long reportId, ReconciliationDetailQueryDTO dto);

    /** 标记差异处理状态 */
    void markDetailProcessStatus(Long detailId, String processStatus, String processRemark, String operator);

    /** 标记报告处理完成 */
    void resolveReport(Long reportId, String operator);

    /** 重新生成对账文件 */
    ReconciliationReportVO regenerateReport(LocalDate reportDate, String operator);
}
