package com.zns.positioning.positioningmanagement.controller.admin;

import com.zns.positioning.positioningmanagement.common.R;
import com.zns.positioning.positioningmanagement.entity.DailyFlow;
import com.zns.positioning.positioningmanagement.service.DailyFlowService;
import com.zns.positioning.positioningmanagement.task.DailyFlowTask;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 每日流水管理 — 后台接口（用于流水确认/对账）
 */
@RestController
@RequestMapping("/admin/daily-flow")
public class ReconciliationController {

    @Resource
    private DailyFlowService dailyFlowService;

    @Resource
    private DailyFlowTask dailyFlowTask;

    /**
     * 手动生成指定日期的每日流水
     */
    @PostMapping("/generate")
    public R<DailyFlow> generate(@RequestParam(required = false)
                                 @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now().minusDays(1);
        DailyFlow flow = dailyFlowService.generateDailyFlow(targetDate);
        return R.ok(flow);
    }

    /**
     * 查询指定日期的每日流水
     */
    @GetMapping("/{date}")
    public R<DailyFlow> getByDate(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        DailyFlow flow = dailyFlowService.getByFlowDate(date);
        return flow != null ? R.ok(flow) : R.fail("该日期暂无流水记录");
    }

    /**
     * 手动触发定时清洗任务（生成昨天的每日流水）
     */
    @PostMapping("/run-task")
    public R<String> runTask() {
        dailyFlowTask.generateYesterdayFlow();
        return R.ok("定时任务已触发，正在生成昨日流水");
    }
}
