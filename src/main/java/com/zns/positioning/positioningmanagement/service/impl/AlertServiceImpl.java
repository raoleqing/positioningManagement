package com.zns.positioning.positioningmanagement.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zns.positioning.positioningmanagement.entity.AlertConfig;
import com.zns.positioning.positioningmanagement.service.AlertConfigService;
import com.zns.positioning.positioningmanagement.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 告警发送实现 — 钉钉Webhook + 邮件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertConfigService alertConfigService;

    @jakarta.annotation.Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Override
    public void sendAlert(String alertType, String title, String content) {
        AlertConfig config = alertConfigService.getByAlertType(alertType);
        if (config == null) {
            log.warn("未找到告警配置, alertType={}", alertType);
            return;
        }

        // 异步发送，避免阻塞定时任务
        if (config.getDingtalkEnabled() != null && config.getDingtalkEnabled() == 1) {
            CompletableFuture.runAsync(() -> sendDingTalk(config.getDingtalkWebhookUrl(), title, content));
        }
        if (config.getEmailEnabled() != null && config.getEmailEnabled() == 1) {
            CompletableFuture.runAsync(() -> sendEmail(config.getEmailRecipients(), title, content));
        }
    }

    /**
     * 发送钉钉webhook消息
     */
    private void sendDingTalk(String webhookUrl, String title, String content) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }
        try {
            JSONObject body = JSONUtil.createObj()
                    .set("msgtype", "markdown")
                    .set("markdown", JSONUtil.createObj()
                            .set("title", title)
                            .set("text", "## " + title + "\n\n" + content));

            try (HttpResponse response = HttpRequest.post(webhookUrl)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(body.toString())
                    .timeout(10000)
                    .execute()) {
                log.info("钉钉告警发送结果, url={}, status={}, body={}",
                        webhookUrl.replaceAll("access_token=[^&]+", "access_token=***"),
                        response.getStatus(), response.body());
            }
        } catch (Exception e) {
            log.error("钉钉告警发送异常", e);
        }
    }

    /**
     * 发送邮件
     */
    private void sendEmail(String recipients, String title, String content) {
        if (recipients == null || recipients.isEmpty() || mailFrom.isEmpty()) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(recipients.split(","));
            message.setSubject(title);
            message.setText(content);
            javaMailSender.send(message);
            log.info("邮件告警发送成功, to={}, title={}", recipients, title);
        } catch (Exception e) {
            log.error("邮件告警发送异常, to={}, title={}", recipients, title, e);
        }
    }
}
