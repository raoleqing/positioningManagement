package com.zns.positioning.positioningmanagement.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zns.positioning.positioningmanagement.entity.OperationLog;
import com.zns.positioning.positioningmanagement.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 操作日志清理任务
 * 每天凌晨3点自动清理6个月之前的操作日志，确保数据库空间可控
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogCleanupTask {

    private final OperationLogMapper operationLogMapper;

    /**
     * 每天凌晨 3:00 执行，清理 6 个月之前的日志
     * 保留 185 天（约 6 个月），留有余量
     */
    @Async
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cleanupOldLogs() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusDays(185);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(OperationLog::getCreateTime, sixMonthsAgo);
        long deletedCount = operationLogMapper.delete(wrapper);
        if (deletedCount > 0) {
            log.info("[日志清理] 已清理 {} 天前的操作日志，共删除 {} 条", 185, deletedCount);
        }
    }
}
