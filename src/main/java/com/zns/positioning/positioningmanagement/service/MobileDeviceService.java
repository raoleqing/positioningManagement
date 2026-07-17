package com.zns.positioning.positioningmanagement.service;

import com.zns.positioning.positioningmanagement.vo.DeviceDetailVO;
import com.zns.positioning.positioningmanagement.vo.DeviceSimplifyVO;

import java.util.List;

/**
 * 移动端-设备 Service
 */
public interface MobileDeviceService {

    /**
     * 查询用户的设备列表
     *
     * @param userId 用户ID
     * @return 设备列表（含主控标识）
     */
    List<DeviceSimplifyVO> listUserDevices(Integer userId);

    /**
     * 查询设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情（含附加属性、绑定用户）
     */
    DeviceDetailVO getDeviceDetail(Integer deviceId);
}
