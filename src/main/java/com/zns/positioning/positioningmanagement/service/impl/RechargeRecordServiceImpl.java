package com.zns.positioning.positioningmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.common.enums.OperationLogTypeEnum;
import com.zns.positioning.positioningmanagement.common.enums.PayStatusEnum;
import com.zns.positioning.positioningmanagement.entity.RechargeOrder;
import com.zns.positioning.positioningmanagement.entity.RechargeRecord;
import com.zns.positioning.positioningmanagement.mapper.RechargeOrderMapper;
import com.zns.positioning.positioningmanagement.mapper.RechargeRecordMapper;
import com.zns.positioning.positioningmanagement.service.OperationLogService;
import com.zns.positioning.positioningmanagement.service.RechargeRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 充值记录 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeRecordServiceImpl implements RechargeRecordService {

    private final RechargeRecordMapper rechargeRecordMapper;
    private final RechargeOrderMapper rechargeOrderMapper;
    private final OperationLogService operationLogService;

    @Override
    public void saveRecord(RechargeRecord record) {
        rechargeRecordMapper.insert(record);
        log.info("保存充值记录成功, orderNo={}, status={}", record.getOrderNo(), record.getRechargeStatus());
    }

    @Override
    public Page<RechargeRecord> queryPage(int pageNum, int pageSize,
                                          String orderNo, String rechargeStatus,
                                          String startTime, String endTime) {
        Page<RechargeRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RechargeRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.eq(RechargeRecord::getOrderNo, orderNo);
        }
        if (StringUtils.hasText(rechargeStatus)) {
            wrapper.eq(RechargeRecord::getRechargeStatus, rechargeStatus);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(RechargeRecord::getCreateTime, startTime);
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(RechargeRecord::getCreateTime, endTime);
        }
        wrapper.orderByDesc(RechargeRecord::getCreateTime);
        return rechargeRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public RechargeRecord getById(Long id) {
        return rechargeRecordMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryRecharge(Long recordId, String operator) {
        RechargeRecord record = rechargeRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("充值记录不存在");
        }
        Long orderId = record.getOrderId();
        RechargeOrder order = rechargeOrderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new RuntimeException("关联订单不存在");
        }
        // 将订单充值状态重置，触发重新扣费
        order.setRechargeStatus("RETRYING");
        order.setRetryCount(order.getRetryCount() != null ? order.getRetryCount() + 1 : 1);
        order.setErrorMsg(null);
        rechargeOrderMapper.updateById(order);

        operationLogService.saveLog(OperationLogTypeEnum.ORDER_MANUAL_RETRY,
                order.getId(), order.getOrderNo(), order.getDeviceNo(),
                "INFO", "预存款明细-手动重试",
                "操作人: " + operator + ", 第" + order.getRetryCount() + "次重试", operator);

        log.info("重试充值触发成功, recordId={}, orderNo={}, operator={}", recordId, order.getOrderNo(), operator);

        // 触发充值

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long recordId, String operator) {
        RechargeRecord record = rechargeRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("充值记录不存在");
        }
        Long orderId = record.getOrderId();
        RechargeOrder order = rechargeOrderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new RuntimeException("关联订单不存在");
        }
        order.setPayStatus(PayStatusEnum.REFUNDED.name());
        order.setRechargeStatus("REFUNDED");
        order.setUpdateTime(LocalDateTime.now());
        rechargeOrderMapper.updateById(order);

        operationLogService.saveLog(OperationLogTypeEnum.ORDER_PAY_CALLBACK,
                order.getId(), order.getOrderNo(), order.getDeviceNo(),
                "INFO", "预存款明细-退款",
                "操作人: " + operator + ", 金额: " + order.getPlanAmount(), operator);

        log.info("退款处理成功, recordId={}, orderNo={}, operator={}", recordId, order.getOrderNo(), operator);
    }

    @Override
    public List<RechargeRecord> getByOrderId(Long orderId) {
        LambdaQueryWrapper<RechargeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeRecord::getOrderId, orderId)
                .orderByDesc(RechargeRecord::getCreateTime);
        return rechargeRecordMapper.selectList(wrapper);
    }

    @Override
    public List<RechargeRecord> getByOrderNo(String orderNo) {
        LambdaQueryWrapper<RechargeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeRecord::getOrderNo, orderNo)
                .orderByDesc(RechargeRecord::getCreateTime);
        return rechargeRecordMapper.selectList(wrapper);
    }
}
