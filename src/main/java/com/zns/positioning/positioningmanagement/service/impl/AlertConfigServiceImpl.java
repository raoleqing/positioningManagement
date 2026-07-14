package com.zns.positioning.positioningmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zns.positioning.positioningmanagement.entity.AlertConfig;
import com.zns.positioning.positioningmanagement.mapper.AlertConfigMapper;
import com.zns.positioning.positioningmanagement.service.AlertConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertConfigServiceImpl implements AlertConfigService {

    private final AlertConfigMapper alertConfigMapper;

    @Override
    public List<AlertConfig> listAll() {
        return alertConfigMapper.selectList(null);
    }

    @Override
    public AlertConfig getByAlertType(String alertType) {
        return alertConfigMapper.selectOne(
                new LambdaQueryWrapper<AlertConfig>().eq(AlertConfig::getAlertType, alertType));
    }

    @Override
    public AlertConfig saveOrUpdate(AlertConfig config) {
        AlertConfig existing = getByAlertType(config.getAlertType());
        if (existing != null) {
            config.setId(existing.getId());
            alertConfigMapper.updateById(config);
        } else {
            alertConfigMapper.insert(config);
        }
        return config;
    }
}
