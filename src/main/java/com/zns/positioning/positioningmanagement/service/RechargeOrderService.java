package com.zns.positioning.positioningmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.dto.DeviceValidityUpdateDTO;
import com.zns.positioning.positioningmanagement.dto.OrderQueryDTO;
import com.zns.positioning.positioningmanagement.entity.OrderLog;
import com.zns.positioning.positioningmanagement.entity.ValidityUpdateLog;
import com.zns.positioning.positioningmanagement.vo.DeviceValidityVO;
import com.zns.positioning.positioningmanagement.vo.OrderVO;

import java.util.List;

public interface RechargeOrderService {

    /** 分页查询订单列表 */
    Page<OrderVO> queryOrders(OrderQueryDTO dto);

    /** 查看订单详细日志 */
    List<OrderLog> getOrderLogs(Long orderId);

    /** 手动重试单个失败订单 */
    void retryOrder(Long orderId, String operator);

    /** 一键重试所有失败订单 */
    int retryAllFailedOrders(String operator);

    /** 手动修正设备有效期 */
    void updateDeviceValidity(DeviceValidityUpdateDTO dto, String operator);

    /** 查看设备有效期修正日志 */
    List<ValidityUpdateLog> getValidityUpdateLogs(Long deviceId);

    /** 获取设备有效期 */
    DeviceValidityVO getDeviceValidity(Long deviceId);
}
