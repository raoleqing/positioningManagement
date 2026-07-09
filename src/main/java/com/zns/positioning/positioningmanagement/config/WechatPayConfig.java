package com.zns.positioning.positioningmanagement.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
     * 需要设置环境变量：WECHAT_MCH_ID, WECHAT_MCH_SERIAL_NO, WECHAT_API_V3_KEY, WECHAT_PRIVATE_KEY_PATH
     */
    @Bean
    @ConditionalOnExpression("!${wechat.pay.mock:false} and '${wechat.pay.privateKeyPath:}' != ''")
    public Config wechatPaySdkConfig() {
        if (privateKeyPath == null || privateKeyPath.isEmpty()) {
            throw new IllegalStateException("微信支付私钥路径未配置，请设置 wechat.pay.privateKeyPath 或启用模拟模式 wechat.pay.mock=true");
        }
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(mchId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(mchSerialNo)
                .apiV3Key(apiV3Key)
                .build();
    }
}
