package com.zns.positioning.positioningmanagement.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * 签名工具类
 * <p>
 * 签名算法：
 * 1. 所有请求参数按参数名升序排序
 * 2. 按参数名 + 参数值拼接字符串
 * 3. 将 appKey 分别添加到字符串头部和尾部
 * 4. SHA1 运算得到二进制数组
 * 5. 转十六进制大写字符串
 */
public class SignUtil {

    /**
     * 生成签名
     *
     * @param params 请求参数（不含 sign 字段）
     * @param appKey 应用密钥
     * @return 大写十六进制签名字符串
     */
    public static String sign(Map<String, String> params, String appKey) {
        // 1. 按参数名升序排序（TreeMap 默认自然排序）
        TreeMap<String, String> sortedParams = new TreeMap<>(params);

        // 2. 拼接参数名和参数值
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            // 跳过空值和 sign 字段本身
            String value = entry.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            sb.append(entry.getKey()).append(value);
        }

        // 3. 头部和尾部添加 appKey
        String signStr = appKey + sb.toString() + appKey;

        // 4. SHA1 运算
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(signStr.getBytes(StandardCharsets.UTF_8));

            // 5. 转大写十六进制字符串
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1算法不可用", e);
        }
    }

    /**
     * 字节数组转大写十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
}
