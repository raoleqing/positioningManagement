package com.zns.positioning.positioningmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.dto.CreateOrderDTO;
import com.zns.positioning.positioningmanagement.dto.MobileOrderQueryDTO;
import com.zns.positioning.positioningmanagement.vo.DeviceSimplifyVO;
import com.zns.positioning.positioningmanagement.vo.MobileOrderDetailVO;

import java.util.Map;

/**
 * 小程序端-充值服务
 */
public interface MobileRechargeService {

    /**
     * 创建充值订单
     */
    MobileOrderDetailVO createOrder(CreateOrderDTO dto);

    /**
     * 微信统一下单（获取预支付参数）
     * @return 小程序调起支付所需的参数
     */
    Map<String, String> unifiedOrder(Long orderId, String openid);

    /**
     * 支付回调处理
     */
    String handlePayCallback(String body, String signature, String serial, String timestamp, String nonce);

    /**
     * 用户充值记录分页
     */
    Page<MobileOrderDetailVO> queryUserOrders(MobileOrderQueryDTO dto);

    /**
     * 获取设备有效期信息
     */
    DeviceSimplifyVO getDeviceValidity(Long deviceId);
}
