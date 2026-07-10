package com.zns.positioning.positioningmanagement.dto;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * 运营商API统一响应格式
 * <p>
 * 接口返回格式：{ "code": "xxx", "msg": "xxx", "data": {...} }
 */
@Data
public class OperatorApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 响应码（0 或 "SUCCESS" 表示成功） */
    private String code;

    /** 响应消息 */
    private String msg;

    /** 响应数据 */
    private T data;

    /**
     * 从 JSONObject 构建响应对象，data 保持为 JSONObject
     */
    public static OperatorApiResponse<JSONObject> fromJson(JSONObject json) {
        OperatorApiResponse<JSONObject> resp = new OperatorApiResponse<>();
        resp.setCode(json.getStr("code"));
        resp.setMsg(json.getStr("msg"));
        resp.setData(json.getJSONObject("data"));
        return resp;
    }

    /**
     * 从 JSON 字符串构建响应对象
     */
    public static OperatorApiResponse<JSONObject> fromJson(String jsonStr) {
        return fromJson(JSONUtil.parseObj(jsonStr));
    }

    /**
     * 是否调用成功
     */
    public boolean isSuccess() {
        return "0".equals(code) || "SUCCESS".equalsIgnoreCase(code);
    }

    /**
     * 是否调用失败
     */
    public boolean isFail() {
        return !isSuccess();
    }
}
