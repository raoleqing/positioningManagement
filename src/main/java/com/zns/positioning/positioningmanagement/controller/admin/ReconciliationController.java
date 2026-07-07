package com.zns.positioning.positioningmanagement.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.common.R;
import com.zns.positioning.positioningmanagement.dto.ReconciliationDetailQueryDTO;
import com.zns.positioning.positioningmanagement.dto.ReconciliationGenerateDTO;
import com.zns.positioning.positioningmanagement.dto.ReconciliationQueryDTO;
import com.zns.positioning.positioningmanagement.service.ReconciliationService;
import com.zns.positioning.positioningmanagement.vo.ReconciliationDetailVO;
import com.zns.positioning.positioningmanagement.vo.ReconciliationReportVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    /** 生成对账报告 */
    @PostMapping("/generate")
    public R<ReconciliationReportVO> generate(@Valid @RequestBody ReconciliationGenerateDTO dto,
                                               @RequestParam String operator) {
        return R.ok(reconciliationService.generateReport(dto, operator));
    }

    /** 查询对账报告列表 */
    @GetMapping("/reports")
    public R<Page<ReconciliationReportVO>> queryReports(ReconciliationQueryDTO dto) {
        return R.ok(reconciliationService.queryReports(dto));
    }

    /** 查看对账差异明细 */
    @GetMapping("/reports/{reportId}/details")
    public R<Page<ReconciliationDetailVO>> queryDetails(@PathVariable Long reportId,
                                                         ReconciliationDetailQueryDTO dto) {
        return R.ok(reconciliationService.queryReportDetails(reportId, dto));
    }

    /** 标记差异处理状态 */
    @PutMapping("/details/{detailId}/status")
    public R<Void> markDetailStatus(@PathVariable Long detailId,
                                     @RequestParam String processStatus,
                                     @RequestParam String processRemark,
                                     @RequestParam String operator) {
        reconciliationService.markDetailProcessStatus(detailId, processStatus, processRemark, operator);
        return R.ok();
    }

    /** 标记报告已处理 */
    @PutMapping("/reports/{reportId}/resolve")
    public R<Void> resolveReport(@PathVariable Long reportId, @RequestParam String operator) {
        reconciliationService.resolveReport(reportId, operator);
        return R.ok();
    }

    /** 重新生成对账文件 */
    @PostMapping("/reports/regenerate")
    public R<ReconciliationReportVO> regenerate(@RequestParam LocalDate reportDate,
                                                 @RequestParam String operator) {
        return R.ok(reconciliationService.regenerateReport(reportDate, operator));
    }
}
