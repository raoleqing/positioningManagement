package com.zns.positioning.positioningmanagement.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

  @Autowired
  private RedisTemplate<Object, Object> redisTemplate;

  public boolean set(String redisKey, Object redisValue) {
    redisTemplate.opsForValue().set(redisKey, redisValue);
    return true;
  }

  public String get(String redisKey) {
    Object obj = redisTemplate.opsForValue().get(redisKey);
    if (null == obj) {
      return "";
    }

    return String.valueOf(obj);
  }

  public Long incr(String redisKey) {
    return redisTemplate.opsForValue().increment(redisKey);
  }

  public Double incrBy(String redisKey, double delta) {
    return redisTemplate.opsForValue().increment(redisKey, delta);
  }

  public Long decrement(String redisKey) {
    return redisTemplate.opsForValue().decrement(redisKey);
  }

  public Long decrBy(String redisKey, long delta) {
    return redisTemplate.opsForValue().decrement(redisKey, delta);
  }

  /**
   * 向Redis设置一个带有过期时间的键值对（原子操作）
   * @param redisKey 键
   * @param redisValue 值
   * @param timeSeconds 过期时间（秒）
   * @return true-设置成功, false-设置失败
   */
  public boolean setex(String redisKey, Object redisValue, long timeSeconds) {
    try {
      // 使用 setEx 方法，原子性地完成 set + expire
      redisTemplate.opsForValue().set(redisKey, redisValue, timeSeconds, TimeUnit.SECONDS);
      return true;
    } catch (Exception e) {
      // 返回false告知调用方失败
      return false;
    }
  }

  /**
   * setnx 将 key 的值设为 value,当且仅当 key 不存在
   * 
   * @param redisKey
   * @param redisValue
   * @param timeSeconds
   * @return
   */
  public boolean setnx(String redisKey, Object redisValue, long timeSeconds) {
    return redisTemplate.opsForValue().setIfAbsent(redisKey, redisValue, timeSeconds, TimeUnit.SECONDS);
  }

  public boolean del(String redisKey) {
    return redisTemplate.delete(redisKey);
  }

  public boolean hdel(String redisKey, String field) {
    return redisTemplate.opsForHash().delete(redisKey, field) == 0;
  }

  public boolean expire(String redisKey, long time, TimeUnit unit) {
    return redisTemplate.expire(redisKey, time, unit);
  }

  public boolean expireAt(String redisKey, Date date) {
    return redisTemplate.expireAt(redisKey, date);
  }

  public boolean hset(String redisKey, String field, String value) {
    redisTemplate.opsForHash().put(redisKey, field, value);
    return true;
  }

  public boolean hset(String redisKey, String field, int value) {
    redisTemplate.opsForHash().put(redisKey, field, value);
    return true;
  }

  public String hget(String redisKey, String field) {
    return (String) redisTemplate.opsForHash().get(redisKey, field);
  }

  public Object hgetObject(String redisKey, String field) {
    return redisTemplate.opsForHash().get(redisKey, field);

  }

  public Map<Object, Object> hgetAll(String redisKey) {
    return (Map<Object, Object>) redisTemplate.opsForHash().entries(redisKey);
  }


  public boolean exists(String redisKey) {
    return redisTemplate.hasKey(redisKey);
  }

  public long sadd(String redisKey, String value) {
    return redisTemplate.opsForSet().add(redisKey, value);
  }

  public boolean sismember(String redisKey, String value) {
    return redisTemplate.opsForSet().isMember(redisKey, value);
  }

  public Set<Object> members(String redisKey) {
    return redisTemplate.opsForSet().members(redisKey);
  }

  public Long srem(String redisKey, String... value) {
    return redisTemplate.opsForSet().remove(redisKey, value);
  }

  public Object randomMembers(String redisKey) {
    List<Object> objList = redisTemplate.opsForSet().randomMembers(redisKey, 1);
    if (objList == null || objList.isEmpty()) {
      return null;
    }
    return objList.get(0);
  }

  public long scard(String redisKey) {
    Long num = redisTemplate.opsForSet().size(redisKey);
    return num == null ? 0 : num;
  }


  public long hincrby(String redisKey, String field, long number) {
    return redisTemplate.opsForHash().increment(redisKey, field, number);
  }



}
