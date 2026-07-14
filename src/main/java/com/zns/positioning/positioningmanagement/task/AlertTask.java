package com.zns.positioning.positioningmanagement.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zns.positioning.positioningmanagement.common.enums.RechargeStatusEnum;
import com.zns.positioning.positioningmanagement.common.redis.RedisService;
import com.zns.positioning.positioningmanagement.entity.RechargeOrder;
import com.zns.positioning.positioningmanagement.mapper.RechargeOrderMapper;
import com.zns.positioning.positioningmanagement.service.AlertService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 告警定时任务 — 每5分钟检查一次
 * <ul>
 *   <li>充值接口连续失败（retryCount >= maxRetryCount）</li>
 *   <li>预存款不足（充值失败原因包含"余额不足"/"欠停"）</li>
 * </ul>
 */
@Slf4j
@Component
public class AlertTask {

    @Resource
    private RechargeOrderMapper rechargeOrderMapper;

    @Resource
    private AlertService alertService;

    @Resource
    private RedisService redisService;

    /** 告警去重 key 前缀 */
    private static final String ALERT_DEDUP_PREFIX = "alert:dedup:";
    /** 去重有效期（秒），避免同一订单重复告警 */
    private static final long DEDUP_TTL = 600;

    /**
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkAndAlert() {
        log.info("========== 告警定时任务开始 ==========");
        try {
            checkRechargeFail();
            checkBalanceLow();
        } catch (Exception e) {
            log.error("告警定时任务执行异常", e);
        }
        log.info("========== 告警定时任务结束 ==========");
    }

    /**
     * 检查充值接口连续失败（所有重试已耗尽）
     */
    private void checkRechargeFail() {
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(10);
        List<RechargeOrder> failedOrders = rechargeOrderMapper.selectList(
                new LambdaQueryWrapper<RechargeOrder>()
                        .eq(RechargeOrder::getRechargeStatus, RechargeStatusEnum.FAILED.name())
                        .apply("retry_count >= max_retry_count")
                        .ge(RechargeOrder::getUpdateTime, recentTime));

        if (failedOrders.isEmpty()) {
            return;
        }

        // 去重：对于已告警的订单不再重复发送
        List<RechargeOrder> newFails = failedOrders.stream()
                .filter(o -> {
                    String key = ALERT_DEDUP_PREFIX + "recharge_fail:" + o.getId();
                    return redisService.setnx(key, "1", DEDUP_TTL);
                })
                .toList();

        if (newFails.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("以下充值订单接口连续失败（重试次数已耗尽），请及时处理：\n\n");
        sb.append("| 订单号 | 设备编号 | ICCID | 套餐 | 失败原因 |\n");
        sb.append("|--------|----------|-------|------|----------|\n");
        for (RechargeOrder o : newFails) {
            String err = o.getErrorMsg() != null ? truncate(o.getErrorMsg(), 30) : "未知";
            sb.append("| ").append(o.getOrderNo())
                    .append(" | ").append(o.getDeviceNo() != null ? o.getDeviceNo() : "-")
                    .append(" | ").append(o.getIccid() != null ? o.getIccid() : "-")
                    .append(" | ").append(o.getPlanName() != null ? o.getPlanName() : "-")
                    .append(" | ").append(err)
                    .append(" |\n");
        }

        alertService.sendAlert("RECHARGE_FAIL",
                "【告警】充值接口连续失败 - " + newFails.size() + "笔",
                sb.toString());
        log.info("充值失败告警已发送, 订单数={}", newFails.size());
    }

    /**
     * 检查预存款不足（充值失败原因包含余额不足关键字）
     */
    private void checkBalanceLow() {
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(10);
        List<RechargeOrder> balanceLowOrders = rechargeOrderMapper.selectList(
                new LambdaQueryWrapper<RechargeOrder>()
                        .eq(RechargeOrder::getRechargeStatus, RechargeStatusEnum.FAILED.name())
                        .ge(RechargeOrder::getUpdateTime, recentTime)
                        .and(w -> w.like(RechargeOrder::getErrorMsg, "余额不足")
                                .or().like(RechargeOrder::getErrorMsg, "欠停")
                                .or().like(RechargeOrder::getErrorMsg, "欠费")
                                .or().like(RechargeOrder::getErrorMsg, "预存款")));

        if (balanceLowOrders.isEmpty()) {
            return;
        }

        // 去重
        List<RechargeOrder> newLows = balanceLowOrders.stream()
                .filter(o -> {
                    String key = ALERT_DEDUP_PREFIX + "balance_low:" + o.getId();
                    return redisService.setnx(key, "1", DEDUP_TTL);
                })
                .toList();

        if (newLows.isEmpty()) {
            return;
        }

        // 按 ICCID 分组统计
        Set<String> affectedIccid = newLows.stream()
                .map(RechargeOrder::getIccid)
                .filter(iccid -> iccid != null)
                .collect(Collectors.toSet());

        StringBuilder sb = new StringBuilder();
        sb.append("以下SIM卡出现预存款不足，导致充值失败，请及时充值：\n\n");
        sb.append("| 订单号 | ICCID | 设备编号 | 失败原因 |\n");
        sb.append("|--------|-------|----------|----------|\n");
        for (RechargeOrder o : newLows) {
            String err = o.getErrorMsg() != null ? truncate(o.getErrorMsg(), 30) : "未知";
            sb.append("| ").append(o.getOrderNo())
                    .append(" | ").append(o.getIccid() != null ? o.getIccid() : "-")
                    .append(" | ").append(o.getDeviceNo() != null ? o.getDeviceNo() : "-")
                    .append(" | ").append(err)
                    .append(" |\n");
        }
        sb.append("\n> 共 ").append(affectedIccid.size()).append(" 张SIM卡预存款不足");

        alertService.sendAlert("BALANCE_LOW",
                "【告警】预存款不足 - " + newLows.size() + "笔",
                sb.toString());
        log.info("预存款不足告警已发送, 订单数={}, 卡数={}", newLows.size(), affectedIccid.size());
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "-";
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }
}
