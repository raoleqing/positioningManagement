package com.zns.positioning.positioningmanagement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zns.positioning.positioningmanagement.common.enums.OperationLogTypeEnum;
import com.zns.positioning.positioningmanagement.entity.OperationLog;
import com.zns.positioning.positioningmanagement.mapper.OperationLogMapper;
import com.zns.positioning.positioningmanagement.service.OperationLogService;
import com.zns.positioning.positioningmanagement.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用操作日志 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    @Override
    public void saveLog(OperationLogTypeEnum logType, Long businessId, String businessNo,
                        String businessName, String logLevel, String title,
                        String content, String operator) {
        OperationLog entity = new OperationLog();
        entity.setBusinessType(logType.getBusinessType());
        entity.setBusinessId(businessId);
        entity.setBusinessNo(businessNo);
        entity.setBusinessName(businessName);
        entity.setLogType(logType.name());
        entity.setLogLevel(logLevel);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setOperator(operator);
        operationLogMapper.insert(entity);
    }

    @Override
    public List<OperationLogVO> getLogs(String businessType, Long businessId) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLog::getBusinessType, businessType)
                .eq(OperationLog::getBusinessId, businessId)
                .orderByDesc(OperationLog::getCreateTime);
        List<OperationLog> logs = operationLogMapper.selectList(wrapper);
        List<OperationLogVO> voList = new ArrayList<>();
        for (OperationLog log : logs) {
            OperationLogVO vo = new OperationLogVO();
            BeanUtil.copyProperties(log, vo);
            vo.setLogTypeText(getLogTypeText(log.getLogType()));
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 根据 logType 枚举值获取中文描述
     */
    private String getLogTypeText(String logType) {
        try {
            return OperationLogTypeEnum.valueOf(logType).getDesc();
        } catch (IllegalArgumentException e) {
            return logType;
        }
    }
}
