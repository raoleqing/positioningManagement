package com.zns.positioning.positioningmanagement.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信支付配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayConfig {

    /** 开发环境模拟模式 */
    private boolean mock = false;

    /** 公众号/小程序 AppID */
    private String appId;

    /** 商户号 */
    private String mchId;

    /** 商户API证书序列号 */
    private String mchSerialNo;

    /** 商户APIv3密钥 */
    private String apiV3Key;

    /** 商户私钥路径 */
    private String privateKeyPath;

    /** 支付回调地址 */
    private String notifyUrl;

    /**
     * 微信支付核心配置（非模拟模式时启用）
     */
    @Bean
    public Config wechatPaySdkConfig() {
        if (mock) {
            return null; // 模拟模式，无需SDK配置
        }
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(mchId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(mchSerialNo)
                .apiV3Key(apiV3Key)
                .build();
    }
}
