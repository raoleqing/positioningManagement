package com.zns.positioning.positioningmanagement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.common.enums.OperationLogTypeEnum;
import com.zns.positioning.positioningmanagement.common.enums.RechargeStatusEnum;
import com.zns.positioning.positioningmanagement.dto.OrderQueryDTO;
import com.zns.positioning.positioningmanagement.entity.*;
import com.zns.positioning.positioningmanagement.mapper.*;
import com.zns.positioning.positioningmanagement.service.OperationLogService;
import com.zns.positioning.positioningmanagement.service.RechargeOrderService;
import com.zns.positioning.positioningmanagement.vo.OperationLogVO;
import com.zns.positioning.positioningmanagement.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeOrderServiceImpl implements RechargeOrderService {

    private final RechargeOrderMapper rechargeOrderMapper;
    private final OperationLogService operationLogService;

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
    public List<OperationLogVO> getOrderLogs(Long orderId) {
        return operationLogService.getLogs("ORDER", orderId);
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
        operationLogService.saveLog(OperationLogTypeEnum.ORDER_MANUAL_RETRY,
                order.getId(), order.getOrderNo(), order.getDeviceNo(),
                "INFO", "手动重试充值",
                "操作人: " + operator + ", 第" + order.getRetryCount() + "次重试", operator);

        // 模拟调用运营商API充值
        try {
            boolean rechargeResult = simulateOperatorRecharge(order);
            if (rechargeResult) {
                // 充值成功
                order.setRechargeStatus(RechargeStatusEnum.SUCCESS.name());
                order.setRechargeTime(LocalDateTime.now());
                rechargeOrderMapper.updateById(order);

                // 模拟通知客户系统
                notifyCustomerSystem(order);

                operationLogService.saveLog(OperationLogTypeEnum.ORDER_RECHARGE_RESULT,
                        order.getId(), order.getOrderNo(), order.getDeviceNo(),
                        "INFO", "充值成功", "调用运营商API返回成功", operator);
            } else {
                order.setRechargeStatus(RechargeStatusEnum.FAILED.name());
                order.setErrorMsg("运营商API返回充值失败");
                rechargeOrderMapper.updateById(order);

                operationLogService.saveLog(OperationLogTypeEnum.ORDER_RECHARGE_RESULT,
                        order.getId(), order.getOrderNo(), order.getDeviceNo(),
                        "ERROR", "充值失败", "运营商API返回充值失败", operator);
            }
        } catch (Exception e) {
            log.error("重试充值异常, orderId={}", orderId, e);
            order.setRechargeStatus(RechargeStatusEnum.FAILED.name());
            order.setErrorMsg("重试异常: " + e.getMessage());
            rechargeOrderMapper.updateById(order);

            operationLogService.saveLog(OperationLogTypeEnum.ORDER_RECHARGE_RESULT,
                    order.getId(), order.getOrderNo(), order.getDeviceNo(),
                    "ERROR", "充值异常", "异常信息: " + e.getMessage(), operator);
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

        operationLogService.saveLog(OperationLogTypeEnum.ORDER_MANUAL_RETRY,
                null, null, null,
                "INFO", "一键重试完成",
                "操作人: " + operator + ", 成功: " + successCount + ", 失败: " + failCount, operator);

        return successCount;
    }

    // ============ 私有方法 ============

    private void notifyCustomerSystem(RechargeOrder order) {
        try {
            // TODO: 实际调用客户系统API同步设备有效期
            log.info("通知客户系统更新设备有效期, orderNo={}, deviceId={}", order.getOrderNo(), order.getDeviceId());
            order.setNotifyStatus("SUCCESS");
            order.setNotifyTime(LocalDateTime.now());
            rechargeOrderMapper.updateById(order);

            operationLogService.saveLog(OperationLogTypeEnum.ORDER_NOTIFY_CUSTOMER,
                    order.getId(), order.getOrderNo(), order.getDeviceNo(),
                    "INFO", "通知客户系统成功",
                    "通知客户系统更新设备 " + order.getDeviceId() + " 的有效期", "SYSTEM");
        } catch (Exception e) {
            log.error("通知客户系统失败, orderNo={}", order.getOrderNo(), e);
            order.setNotifyStatus("FAILED");
            rechargeOrderMapper.updateById(order);

            operationLogService.saveLog(OperationLogTypeEnum.ORDER_NOTIFY_CUSTOMER,
                    order.getId(), order.getOrderNo(), order.getDeviceNo(),
                    "ERROR", "通知客户系统失败", "异常: " + e.getMessage(), "SYSTEM");
        }
    }

    private boolean simulateOperatorRecharge(RechargeOrder order) {
        // TODO: 实际对接运营商API
        log.info("模拟调用运营商API充值, orderNo={}, amount={}", order.getOrderNo(), order.getPlanAmount());
        return true;
    }
}
