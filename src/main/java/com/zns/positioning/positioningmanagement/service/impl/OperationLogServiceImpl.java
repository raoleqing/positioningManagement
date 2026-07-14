package com.zns.positioning.positioningmanagement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.common.enums.OperationLogTypeEnum;
import com.zns.positioning.positioningmanagement.entity.OperationLog;
import com.zns.positioning.positioningmanagement.mapper.OperationLogMapper;
import com.zns.positioning.positioningmanagement.service.OperationLogService;
import com.zns.positioning.positioningmanagement.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public Page<OperationLogVO> queryPage(Page<OperationLogVO> page, String businessNo,
                                          String businessType, String logLevel,
                                          LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        // 订单号模糊搜索
        if (StrUtil.isNotBlank(businessNo)) {
            wrapper.like(OperationLog::getBusinessNo, businessNo);
        }
        // 业务类型精确匹配
        if (StrUtil.isNotBlank(businessType)) {
            wrapper.eq(OperationLog::getBusinessType, businessType);
        }
        // 日志级别
        if (StrUtil.isNotBlank(logLevel)) {
            wrapper.eq(OperationLog::getLogLevel, logLevel);
        }
        // 时间范围
        if (startTime != null) {
            wrapper.ge(OperationLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OperationLog::getCreateTime, endTime);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);

        // 先查原始数据
        Page<OperationLog> rawPage = new Page<>(page.getCurrent(), page.getSize());
        Page<OperationLog> resultPage = operationLogMapper.selectPage(rawPage, wrapper);

        // 转换为 VO
        Page<OperationLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<OperationLogVO> voList = resultPage.getRecords().stream().map(log -> {
            OperationLogVO vo = new OperationLogVO();
            BeanUtil.copyProperties(log, vo);
            vo.setLogTypeText(getLogTypeText(log.getLogType()));
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public List<OperationLogVO> queryByBusinessNo(String businessNo) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLog::getBusinessNo, businessNo)
                .orderByAsc(OperationLog::getCreateTime);
        List<OperationLog> logs = operationLogMapper.selectList(wrapper);
        return logs.stream().map(log -> {
            OperationLogVO vo = new OperationLogVO();
            BeanUtil.copyProperties(log, vo);
            vo.setLogTypeText(getLogTypeText(log.getLogType()));
            return vo;
        }).collect(Collectors.toList());
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
