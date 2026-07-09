package com.zns.positioning.positioningmanagement.common.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zns.positioning.positioningmanagement.config.IoTPlatformConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 运营商（物联云平台）API 调用工具
 * <p>
 * 请求方式：POST JSON body（Content-Type: application/json;charset=UTF-8）
 * 签名流程：先签名，再将 sign 加入 JSON body 发送
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperatorApiClient {

    private final IoTPlatformConfig iotConfig;

    /** API 版本 */
    private static final String API_VERSION = "2.0";

    /** 默认超时（毫秒） */
    private static final int TIMEOUT_MS = 30000;

    // ==================== 核心方法 ====================

    /**
     * POST JSON 方式调用运营商接口
     *
     * @param apiMethod 接口方法名（如 queryCardStatus, orderCardPackage）
     * @param bizParams 业务参数（不含系统参数和签名）
     * @return 响应 JSON
     */
    private JSONObject call(String apiMethod, Map<String, Object> bizParams) {
        // 1. 先构建不含 sign 的参数并签名
        Map<String, Object> params = buildParamsWithoutSign(bizParams);
        String sign = SignUtil.sign(toSignMap(params), iotConfig.getAppKey());
        // 2. 将 sign 加入 JSON body
        params.put("sign", sign);

        String url = iotConfig.getBaseUrl() + "/" + apiMethod;
        log.info("调用运营商API [POST_JSON] url={}, body={}", url, JSONUtil.toJsonStr(params));

        try (HttpResponse response = HttpRequest.post(url)
                .timeout(TIMEOUT_MS)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(JSONUtil.toJsonStr(params))
                .execute()) {
            String body = response.body();
            log.info("运营商API响应 url={}, status={}, body={}", url, response.getStatus(), body);
            return JSONUtil.parseObj(body);
        } catch (Exception e) {
            log.error("运营商API调用异常 url={}, params={}", url, params, e);
            throw new RuntimeException("运营商接口调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建请求参数（不含 sign）
     */
    private Map<String, Object> buildParamsWithoutSign(Map<String, Object> bizParams) {
        Map<String, Object> params = new HashMap<>();
        // 公共系统参数
        params.put("appId", iotConfig.getAppId());
        params.put("v", API_VERSION);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        // 业务参数
        if (bizParams != null) {
            params.putAll(bizParams);
        }
        return params;
    }

    /**
     * 将参数转为签名用的 Map&lt;String, String&gt;
     */
    private Map<String, String> toSignMap(Map<String, Object> params) {
        TreeMap<String, String> map = new TreeMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                map.put(entry.getKey(), String.valueOf(value));
            }
        }
        return map;
    }

    // ==================== 业务 API ====================

    /**
     * 查询卡状态
     *
     * @param iccid SIM 卡 ICCID
     * @return 响应 JSON
     */
    public JSONObject queryCardStatus(String iccid) {
        Map<String, Object> params = new HashMap<>();
        params.put("iccid", iccid);
        return call("queryCardStatus", params);
    }

    /**
     * 查询卡流量
     *
     * @param iccid SIM 卡 ICCID
     * @return 响应 JSON
     */
    public JSONObject queryCardTraffic(String iccid) {
        Map<String, Object> params = new HashMap<>();
        params.put("iccid", iccid);
        return call("queryCardTraffic", params);
    }

    /**
     * 查询卡套餐信息
     *
     * @param iccid SIM 卡 ICCID
     * @return 响应 JSON
     */
    public JSONObject queryCardPackage(String iccid) {
        Map<String, Object> params = new HashMap<>();
        params.put("iccid", iccid);
        return call("queryCardPackage", params);
    }

    /**
     * 套餐订购（充值/续费）
     *
     * @param iccid       SIM 卡 ICCID
     * @param packageId   运营商套餐ID
     * @param orderAmount 订购金额（元）
     * @return 响应 JSON
     */
    public JSONObject orderCardPackage(String iccid, String packageId, BigDecimal orderAmount) {
        Map<String, Object> params = new HashMap<>();
        params.put("iccid", iccid);
        params.put("packageId", packageId);
        params.put("orderAmount", orderAmount.toString());
        return call("orderCardPackage", params);
    }

    /**
     * 套餐订购（充值/续费）— 简化版，使用套餐ID
     *
     * @param iccid     SIM 卡 ICCID
     * @param packageId 运营商套餐ID
     * @param amount    充值金额
     * @return 响应 JSON
     */
    public JSONObject recharge(String iccid, String packageId, BigDecimal amount) {
        return orderCardPackage(iccid, packageId, amount);
    }
}
