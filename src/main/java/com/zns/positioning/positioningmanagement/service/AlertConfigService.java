package com.zns.positioning.positioningmanagement.service;

import com.zns.positioning.positioningmanagement.entity.AlertConfig;

import java.util.List;

/**
 * 告警配置服务
 */
public interface AlertConfigService {

    /**
     * 获取所有告警配置
     */
    List<AlertConfig> listAll();

    /**
     * 根据类型获取配置
     */
    AlertConfig getByAlertType(String alertType);

    /**
     * 保存或更新配置
     */
    AlertConfig saveOrUpdate(AlertConfig config);
}
