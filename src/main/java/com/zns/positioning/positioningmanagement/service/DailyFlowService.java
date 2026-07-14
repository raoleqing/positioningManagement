package com.zns.positioning.positioningmanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zns.positioning.positioningmanagement.entity.DailyFlow;

import java.time.LocalDate;

/**
 * 每日流水服务
 */
public interface DailyFlowService extends IService<DailyFlow> {

    /**
     * 生成指定日期的每日流水（汇总充值订单数据）
     *
     * @param date 日期
     * @return 生成的每日流水记录
     */
    DailyFlow generateDailyFlow(LocalDate date);

    /**
     * 查询指定日期的每日流水
     *
     * @param date 日期
     * @return 每日流水记录，不存在则返回null
     */
    DailyFlow getByFlowDate(LocalDate date);
}
