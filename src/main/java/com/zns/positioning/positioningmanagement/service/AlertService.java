package com.zns.positioning.positioningmanagement.service;

/**
 * 告警发送服务 — 通过钉钉Webhook / 邮件发送告警通知
 */
public interface AlertService {

    /**
     * 发送告警
     *
     * @param alertType  告警类型: RECHARGE_FAIL / BALANCE_LOW
     * @param title      告警标题
     * @param content    告警内容（纯文本）
     */
    void sendAlert(String alertType, String title, String content);
}
