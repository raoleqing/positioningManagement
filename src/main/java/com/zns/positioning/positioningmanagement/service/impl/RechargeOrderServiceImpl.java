package com.zns.positioning.positioningmanagement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.common.enums.OrderLogTypeEnum;
import com.zns.positioning.positioningmanagement.common.enums.RechargeStatusEnum;
import com.zns.positioning.positioningmanagement.dto.DeviceValidityUpdateDTO;
import com.zns.positioning.positioningmanagement.dto.OrderQueryDTO;
import com.zns.positioning.positioningmanagement.entity.*;
import com.zns.positioning.positioningmanagement.mapper.*;
import com.zns.positioning.positioningmanagement.service.RechargeOrderService;
import com.zns.positioning.positioningmanagement.vo.DeviceValidityVO;
import com.zns.positioning.positioningmanagement.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeOrderServiceImpl implements RechargeOrderService {

    private final RechargeOrderMapper rechargeOrderMapper;
    private final OrderLogMapper orderLogMapper;
    private final DeviceValidityMapper deviceValidityMapper;
    private final ValidityUpdateLogMapper validityUpdateLogMapper;
    private final PreDepositAccountMapper preDepositAccountMapper;
    private final PreDepositRecordMapper preDepositRecordMapper;

    @Override
    public Page<OrderVO> queryOrders(OrderQueryDTO dto) {
        Page<RechargeOrder> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        var result = rechargeOrderMapper.selectPageByCondition(
                page, dto.getDeviceId(), dto.getUserId(),
                dto.getStartTime(), dto.getEndTime(),
                dto.getOrderNo(), dto.getPayStatus(), dto.getRechargeStatus());

        Page<OrderVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<OrderVO> voList = new ArrayList<>();
        for (RechargeOrder order : result.getRecords()) {
            OrderVO vo = new OrderVO();
            BeanUtil.copyProperties(order, vo);
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<OrderLog> getOrderLogs(Long orderId) {
        RechargeOrder order = rechargeOrderMapper.selectById(orderId);
        if (order == null) {
            return orderLogMapper.selectByOrderId(orderId);
        }
        return orderLogMapper.selectByOrderId(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryOrder(Long orderId, String operator) {
        RechargeOrder order = rechargeOrderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new RuntimeException("订单不存在");
        }
        if (!RechargeStatusEnum.FAILED.name().equals(order.getRechargeStatus())) {
            throw new RuntimeException("仅失败状态的订单可重试");
        }
        if (order.getRetryCount() >= order.getMaxRetryCount()) {
            throw new RuntimeException("已达到最大重试次数");
        }

        // 更新为重试中状态
        order.setRechargeStatus(RechargeStatusEnum.RETRYING.name());
        order.setRetryCount(order.getRetryCount() + 1);
        rechargeOrderMapper.updateById(order);

        // 记录重试日志
        saveLog(order, OrderLogTypeEnum.MANUAL_RETRY, "INFO",
                "手动重试充值", "操作人: " + operator + ", 第" + order.getRetryCount() + "次重试", operator);

        // 模拟调用运营商API充值
        try {
            boolean rechargeResult = simulateOperatorRecharge(order);
            if (rechargeResult) {
                // 充值成功
                order.setRechargeStatus(RechargeStatusEnum.SUCCESS.name());
                order.setRechargeTime(LocalDateTime.now());
                rechargeOrderMapper.updateById(order);

                // 更新预存款账户
                updatePreDepositAccount(order);

                // 更新设备有效期
                updateDeviceValidityAfterRecharge(order);

                // 模拟通知客户系统
                notifyCustomerSystem(order);

                saveLog(order, OrderLogTypeEnum.RECHARGE_RESULT, "INFO",
                        "充值成功", "调用运营商API返回成功", operator);
            } else {
                order.setRechargeStatus(RechargeStatusEnum.FAILED.name());
                order.setErrorMsg("运营商API返回充值失败");
                rechargeOrderMapper.updateById(order);

                saveLog(order, OrderLogTypeEnum.RECHARGE_RESULT, "ERROR",
                        "充值失败", "运营商API返回充值失败", operator);
            }
        } catch (Exception e) {
            log.error("重试充值异常, orderId={}", orderId, e);
            order.setRechargeStatus(RechargeStatusEnum.FAILED.name());
            order.setErrorMsg("重试异常: " + e.getMessage());
            rechargeOrderMapper.updateById(order);

            saveLog(order, OrderLogTypeEnum.RECHARGE_RESULT, "ERROR",
                    "充值异常", "异常信息: " + e.getMessage(), operator);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int retryAllFailedOrders(String operator) {
        List<RechargeOrder> failedOrders = rechargeOrderMapper.selectFailedRetryableOrders();
        int successCount = 0;
        int failCount = 0;

        for (RechargeOrder order : failedOrders) {
            try {
                retryOrder(order.getId(), operator);
                successCount++;
            } catch (Exception e) {
                log.error("一键重试失败, orderId={}", order.getId(), e);
                failCount++;
            }
        }

        saveLog(null, OrderLogTypeEnum.MANUAL_RETRY, "INFO",
                "一键重试完成",
                "操作人: " + operator + ", 成功: " + successCount + ", 失败: " + failCount, operator);

        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceValidity(DeviceValidityUpdateDTO dto, String operator) {
        DeviceValidity validity = deviceValidityMapper.selectByDeviceId(dto.getDeviceId());

        // 记录修正前数据
        ValidityUpdateLog logEntry = new ValidityUpdateLog();
        logEntry.setDeviceId(dto.getDeviceId());

        if (validity != null) {
            logEntry.setDeviceNo(validity.getDeviceNo());
            logEntry.setOldValidFrom(validity.getValidFrom());
            logEntry.setOldValidTo(validity.getValidTo());

            validity.setValidFrom(dto.getValidFrom());
            validity.setValidTo(dto.getValidTo());
            validity.setOperator(operator);
            deviceValidityMapper.updateById(validity);
        } else {
            validity = new DeviceValidity();
            validity.setDeviceId(dto.getDeviceId());
            validity.setValidFrom(dto.getValidFrom());
            validity.setValidTo(dto.getValidTo());
            validity.setOperator(operator);
            validity.setStatus("NORMAL");
            deviceValidityMapper.insert(validity);
        }

        logEntry.setNewValidFrom(dto.getValidFrom());
        logEntry.setNewValidTo(dto.getValidTo());
        logEntry.setReason(dto.getReason());
        logEntry.setOperator(operator);
        validityUpdateLogMapper.insert(logEntry);

        // 同时记录到订单日志
        RechargeOrder order = null;
        var orders = rechargeOrderMapper.selectList(
                new LambdaQueryWrapper<RechargeOrder>()
                        .eq(RechargeOrder::getDeviceId, dto.getDeviceId())
                        .orderByDesc(RechargeOrder::getCreateTime)
                        .last("LIMIT 1"));
        if (orders != null && !orders.isEmpty()) {
            order = orders.get(0);
        }

        if (order != null) {
            saveLog(order, OrderLogTypeEnum.VALIDITY_UPDATE, "INFO",
                    "手动修正设备有效期",
                    "设备ID: " + dto.getDeviceId() + ", 原有效期: " +
                            logEntry.getOldValidTo() + " -> " + dto.getValidTo() +
                            ", 原因: " + dto.getReason(), operator);
        }
    }

    @Override
    public List<ValidityUpdateLog> getValidityUpdateLogs(Long deviceId) {
        return validityUpdateLogMapper.selectByDeviceId(deviceId);
    }

    @Override
    public DeviceValidityVO getDeviceValidity(Long deviceId) {
        DeviceValidity validity = deviceValidityMapper.selectByDeviceId(deviceId);
        if (validity == null) {
            return null;
        }
        DeviceValidityVO vo = new DeviceValidityVO();
        BeanUtil.copyProperties(validity, vo);
        if (validity.getValidTo() != null) {
            vo.setRemainingDays(ChronoUnit.DAYS.between(LocalDate.now(), validity.getValidTo()));
        }
        return vo;
    }

    // ============ 私有方法 ============

    private void updatePreDepositAccount(RechargeOrder order) {
        PreDepositAccount account = preDepositAccountMapper.selectById(1);
        if (account == null) return;

        java.math.BigDecimal beforeBalance = account.getBalance();
        account.setBalance(account.getBalance().subtract(order.getAmount()));
        account.setTotalConsume(account.getTotalConsume().add(order.getAmount()));
        if (account.getBalance().compareTo(new java.math.BigDecimal("500")) < 0) {
            account.setStatus("LOW_BALANCE");
        }
        preDepositAccountMapper.updateById(account);

        // 记录消耗明细
        PreDepositRecord record = new PreDepositRecord();
        record.setAccountId(account.getId());
        record.setOrderId(order.getId());
        record.setOrderNo(order.getOrderNo());
        record.setType("CONSUME");
        record.setAmount(order.getAmount());
        record.setBalanceBefore(beforeBalance);
        record.setBalanceAfter(account.getBalance());
        record.setRemark("设备充值消耗: " + order.getDeviceNo());
        preDepositRecordMapper.insert(record);
    }

    private void updateDeviceValidityAfterRecharge(RechargeOrder order) {
        DeviceValidity validity = deviceValidityMapper.selectByDeviceId(order.getDeviceId());
        LocalDate now = LocalDate.now();

        if (validity == null) {
            validity = new DeviceValidity();
            validity.setDeviceId(order.getDeviceId());
            validity.setDeviceNo(order.getDeviceNo());
            validity.setSimCardNo(order.getSimCardNo());
            validity.setValidFrom(now);
            validity.setValidTo(now.plusYears(order.getYears()));
            validity.setLastRechargeTime(LocalDateTime.now());
            validity.setTotalRechargeAmount(order.getAmount());
            validity.setStatus("NORMAL");
            deviceValidityMapper.insert(validity);
        } else {
            LocalDate newValidTo;
            if (validity.getValidTo() != null && validity.getValidTo().isAfter(now)) {
                // 在现有有效期基础上累加
                newValidTo = validity.getValidTo().plusYears(order.getYears());
            } else {
                newValidTo = now.plusYears(order.getYears());
            }
            validity.setValidTo(newValidTo);
            validity.setValidFrom(validity.getValidFrom() != null ? validity.getValidFrom() : now);
            validity.setLastRechargeTime(LocalDateTime.now());
            validity.setTotalRechargeAmount(
                    validity.getTotalRechargeAmount().add(order.getAmount()));
            validity.setStatus("NORMAL");
            deviceValidityMapper.updateById(validity);
        }
    }

    private void notifyCustomerSystem(RechargeOrder order) {
        try {
            // TODO: 实际调用客户系统API同步设备有效期
            log.info("通知客户系统更新设备有效期, orderNo={}, deviceId={}", order.getOrderNo(), order.getDeviceId());
            order.setNotifyStatus("SUCCESS");
            order.setNotifyTime(LocalDateTime.now());
            rechargeOrderMapper.updateById(order);

            saveLog(order, OrderLogTypeEnum.NOTIFY_CUSTOMER, "INFO",
                    "通知客户系统成功", "通知客户系统更新设备 " + order.getDeviceId() + " 的有效期", "SYSTEM");
        } catch (Exception e) {
            log.error("通知客户系统失败, orderNo={}", order.getOrderNo(), e);
            order.setNotifyStatus("FAILED");
            rechargeOrderMapper.updateById(order);

            saveLog(order, OrderLogTypeEnum.NOTIFY_CUSTOMER, "ERROR",
                    "通知客户系统失败", "异常: " + e.getMessage(), "SYSTEM");
        }
    }

    private boolean simulateOperatorRecharge(RechargeOrder order) {
        // TODO: 实际对接运营商API
        log.info("模拟调用运营商API充值, orderNo={}, amount={}", order.getOrderNo(), order.getAmount());
        return true;
    }

    private void saveLog(RechargeOrder order, OrderLogTypeEnum logType, String level,
                         String title, String content, String operator) {
        OrderLog logEntry = new OrderLog();
        if (order != null) {
            logEntry.setOrderId(order.getId());
            logEntry.setOrderNo(order.getOrderNo());
        }
        logEntry.setLogType(logType.name());
        logEntry.setLogLevel(level);
        logEntry.setTitle(title);
        logEntry.setContent(content);
        logEntry.setOperator(operator);
        orderLogMapper.insert(logEntry);
    }
}
