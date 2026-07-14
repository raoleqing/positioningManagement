package com.zns.positioning.positioningmanagement.common.redis;

public class RedisKeyUtil {

    /**
     * 会员登录商户端app的token信息, redis his类型
     *
     * @param userId
     * @return
     */
    public static String getMemberRedisKeyForMerchantApp(Long userId) {
        return "user:app:token:" + userId;
    }

    // 新增：token黑名单key
    public static String getTokenBlacklistKey(Long userId) {
        return String.format("token:blacklist:%s", userId);
    }

    /**
     * 安全锁
     *
     * @param bizType
     * @param bizId
     * @return
     */
    public static String getLockKey(String bizType, String bizId) {
        return String.format("s:lock:%s:%s", bizType, bizId);
    }

    // ==================== 每日流水相关 ====================

    /**
     * 每日流水生成锁 key
     */
    public static String getDailyFlowLockKey(String date) {
        return String.format("daily:flow:lock:%s", date);
    }

    /**
     * 每日流水缓存 key
     */
    public static String getDailyFlowKey(String date) {
        return String.format("daily:flow:%s", date);
    }

    // ==================== 告警相关 ====================

    /**
     * 告警去重 key
     */
    public static String getAlertDedupKey(String alertType, String bizId) {
        return String.format("alert:dedup:%s:%s", alertType, bizId);
    }

}
