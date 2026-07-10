package com.zns.positioning.positioningmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.entity.RechargeRecord;

import java.util.List;

/**
 * 充值记录 Service
 */
public interface RechargeRecordService {

    /**
     * 保存充值记录（由 executeRecharge 内部调用）
     */
    void saveRecord(RechargeRecord record);

    /**
     * 分页查询充值记录
     */
    Page<RechargeRecord> queryPage(int pageNum, int pageSize,
                                   String orderNo, String rechargeStatus,
                                   String startTime, String endTime);

    /**
     * 根据ID查询单条充值记录
     */
    RechargeRecord getById(Long id);

    /**
     * 重试充值（根据充值记录ID，找到关联订单重新发起扣费）
     */
    void retryRecharge(Long recordId, String operator);

    /**
     * 退款（更新关联订单状态为已退款）
     */
    void refund(Long recordId, String operator);

    /**
     * 根据订单ID查询充值记录列表
     */
    List<RechargeRecord> getByOrderId(Long orderId);

    /**
     * 根据订单号查询充值记录列表
     */
    List<RechargeRecord> getByOrderNo(String orderNo);
}
