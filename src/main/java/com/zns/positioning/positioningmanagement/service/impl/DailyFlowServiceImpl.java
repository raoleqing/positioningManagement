package com.zns.positioning.positioningmanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zns.positioning.positioningmanagement.common.enums.PayStatusEnum;
import com.zns.positioning.positioningmanagement.common.enums.RechargeStatusEnum;
import com.zns.positioning.positioningmanagement.entity.DailyFlow;
import com.zns.positioning.positioningmanagement.entity.RechargeOrder;
import com.zns.positioning.positioningmanagement.mapper.DailyFlowMapper;
import com.zns.positioning.positioningmanagement.mapper.RechargeOrderMapper;
import com.zns.positioning.positioningmanagement.service.DailyFlowService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 每日流水服务实现
 */
@Slf4j
@Service
public class DailyFlowServiceImpl extends ServiceImpl<DailyFlowMapper, DailyFlow> implements DailyFlowService {

    @Resource
    private RechargeOrderMapper rechargeOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyFlow generateDailyFlow(LocalDate date) {
        // 1. 检查是否已存在该日期的流水，存在则跳过
        DailyFlow existingFlow = getByFlowDate(date);
        if (existingFlow != null) {
            log.info("每日流水已存在，跳过生成: {}", date);
            return existingFlow;
        }

        // 2. 查询目标日期的所有充值订单
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        LambdaQueryWrapper<RechargeOrder> wrapper = new LambdaQueryWrapper<RechargeOrder>()
                .ge(RechargeOrder::getCreateTime, startOfDay)
                .le(RechargeOrder::getCreateTime, endOfDay);
        List<RechargeOrder> orders = rechargeOrderMapper.selectList(wrapper);

        // 3. 汇总统计数据
        DailyFlow flow = new DailyFlow();
        flow.setFlowDate(date);

        // 订单总数
        flow.setTotalOrderCount(orders.size());

        // 支付相关统计
        long paidCount = 0;
        BigDecimal paidAmount = BigDecimal.ZERO;
        long refundCount = 0;
        BigDecimal refundAmount = BigDecimal.ZERO;

        // 充值相关统计
        long rechargeSuccessCount = 0;
        BigDecimal rechargeSuccessAmount = BigDecimal.ZERO;
        long rechargeFailedCount = 0;
        BigDecimal rechargeFailedAmount = BigDecimal.ZERO;

        for (RechargeOrder order : orders) {
            BigDecimal amount = order.getPlanAmount() != null ? order.getPlanAmount() : BigDecimal.ZERO;

            // 支付状态统计
            if (PayStatusEnum.PAID.name().equals(order.getPayStatus())) {
                paidCount++;
                paidAmount = paidAmount.add(amount);
            }
            if (PayStatusEnum.REFUNDED.name().equals(order.getPayStatus())) {
                refundCount++;
                refundAmount = refundAmount.add(amount);
            }

            // 充值状态统计
            if (RechargeStatusEnum.SUCCESS.name().equals(order.getRechargeStatus())) {
                rechargeSuccessCount++;
                rechargeSuccessAmount = rechargeSuccessAmount.add(amount);
            } else if (RechargeStatusEnum.FAILED.name().equals(order.getRechargeStatus())) {
                rechargeFailedCount++;
                rechargeFailedAmount = rechargeFailedAmount.add(amount);
            }
        }

        flow.setPaidOrderCount((int) paidCount);
        flow.setPaidAmount(paidAmount);
        flow.setRefundCount((int) refundCount);
        flow.setRefundAmount(refundAmount);
        flow.setRechargeSuccessCount((int) rechargeSuccessCount);
        flow.setRechargeSuccessAmount(rechargeSuccessAmount);
        flow.setRechargeFailedCount((int) rechargeFailedCount);
        flow.setRechargeFailedAmount(rechargeFailedAmount);

        // 4. 保存每日流水
        save(flow);
        log.info("每日流水生成成功: {}, 订单总数={}, 已付金额={}, 充值成功={}", date, orders.size(), paidAmount, rechargeSuccessCount);
        return flow;
    }

    @Override
    public DailyFlow getByFlowDate(LocalDate date) {
        LambdaQueryWrapper<DailyFlow> wrapper = new LambdaQueryWrapper<DailyFlow>()
                .eq(DailyFlow::getFlowDate, date);
        return getOne(wrapper, false);
    }
}
