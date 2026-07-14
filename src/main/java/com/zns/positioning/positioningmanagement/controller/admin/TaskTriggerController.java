package com.zns.positioning.positioningmanagement.controller.admin;

import com.zns.positioning.positioningmanagement.common.R;
import com.zns.positioning.positioningmanagement.task.AlertTask;
import com.zns.positioning.positioningmanagement.task.LogCleanupTask;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定时任务手动触发 Controller
 */
@Slf4j
@RestController
@RequestMapping("/admin/task")
public class TaskTriggerController {

    @Resource
    private AlertTask alertTask;

    @Resource
    private LogCleanupTask logCleanupTask;

    /**
     * 手动触发告警检查任务
     */
    @PostMapping("/alert/trigger")
    public R<String> triggerAlert() {
        log.info("========== 手动触发告警检查任务 ==========");
        alertTask.checkAndAlert();
        return R.ok("告警检查任务已执行完成");
    }

    /**
     * 手动触发日志清理任务
     */
    @PostMapping("/log-cleanup/trigger")
    public R<String> triggerLogCleanup() {
        log.info("========== 手动触发日志清理任务 ==========");
        logCleanupTask.cleanupOldLogs();
        return R.ok("日志清理任务已执行完成");
    }
}
