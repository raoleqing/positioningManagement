package com.zns.positioning.positioningmanagement.controller.admin;

import com.zns.positioning.positioningmanagement.common.R;
import com.zns.positioning.positioningmanagement.entity.AlertConfig;
import com.zns.positioning.positioningmanagement.service.AlertConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 告警配置管理接口
 */
@RestController
@RequestMapping("/api/admin/alert-config")
public class AlertConfigController {

    @Resource
    private AlertConfigService alertConfigService;

    /**
     * 获取所有告警配置
     */
    @GetMapping
    public R<List<AlertConfig>> listAll() {
        return R.ok(alertConfigService.listAll());
    }

    /**
     * 保存/更新告警配置
     */
    @PostMapping
    public R<AlertConfig> save(@RequestBody AlertConfig config) {
        return R.ok(alertConfigService.saveOrUpdate(config));
    }

    /**
     * 根据类型获取配置
     */
    @GetMapping("/{alertType}")
    public R<AlertConfig> getByType(@PathVariable String alertType) {
        AlertConfig config = alertConfigService.getByAlertType(alertType);
        return config != null ? R.ok(config) : R.fail("配置不存在");
    }
}
