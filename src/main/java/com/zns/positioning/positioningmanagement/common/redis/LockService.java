package com.zns.positioning.positioningmanagement.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LockService {

    public final static String USER_CREATE = "user_create";
    public final static String USER_LOGIN = "user_login";
    public final static String USER_UPDATE = "user_update";
    public final static String CREATE_VISIT = "create_visit";
    public final static String UPDATE_VISIT = "update_visit";
    public final static String AUDIT_VISIT = "audit_visit";
    public final static String AUDIT_CREATE = "audit_create";

    @Autowired
    private RedisService redisService;

    public boolean addLock(String bizType, String bizId, long timeSeconds) {
        if (timeSeconds <= 0) {
            timeSeconds = 60 * 10;// 默认60秒
        }
        String redisKey = RedisKeyUtil.getLockKey(bizType, bizId);
        boolean isSuccess = redisService.setnx(redisKey, "1", timeSeconds);
        return isSuccess;
    }

    public boolean removeLock(String bizType, String bizId) {
        String redisKey = RedisKeyUtil.getLockKey(bizType, bizId);
        redisService.del(redisKey);
        return true;
    }

    public boolean existsLock(String bizType, String bizId) {
        String redisKey = RedisKeyUtil.getLockKey(bizType, bizId);
        return redisService.exists(redisKey);
    }
}