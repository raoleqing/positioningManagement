package com.zns.positioning.positioningmanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 物联云平台配置（签名、接口调用）
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "iot.platform")
public class IoTPlatformConfig {

    /** 应用ID（平台分配） */
    private String appId;

    /** 应用密钥（平台分配，用于签名） */
    private String appKey;

    /** API 基础地址 */
    private String baseUrl;
}
