package com.zns.positioning.positioningmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.dto.OrderQueryDTO;
import com.zns.positioning.positioningmanagement.vo.OperationLogVO;
import com.zns.positioning.positioningmanagement.vo.OrderVO;

import java.util.List;

public interface RechargeOrderService {

    /** 分页查询订单列表 */
    Page<OrderVO> queryOrders(OrderQueryDTO dto);

    /** 查看订单详细日志 */
    List<OperationLogVO> getOrderLogs(Long orderId);

    /** 手动重试单个失败订单 */
    void retryOrder(Long orderId, String operator);

    /** 一键重试所有失败订单 */
    int retryAllFailedOrders(String operator);
}
