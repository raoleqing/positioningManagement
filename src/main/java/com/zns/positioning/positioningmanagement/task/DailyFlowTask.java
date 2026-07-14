package com.zns.positioning.positioningmanagement.task;

import com.zns.positioning.positioningmanagement.service.DailyFlowService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 每日流水定时任务 — 每天0点清洗前一天数据，生成每日流水表
 */
@Slf4j
@Component
public class DailyFlowTask {

    @Resource
    private DailyFlowService dailyFlowService;

    /**
     * 每天 00:05 执行，生成前一天的每日流水
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void generateYesterdayFlow() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("========== 开始生成每日流水: {} ==========", yesterday);
        try {
            dailyFlowService.generateDailyFlow(yesterday);
            log.info("========== 每日流水生成完成: {} ==========", yesterday);
        } catch (Exception e) {
            log.error("每日流水生成失败: {}", yesterday, e);
        }
    }
}
