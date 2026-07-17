package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备列表项 VO（移动端）
 */
@Data
public class DeviceSimplifyVO {

    /** 设备ID */
    private Integer deviceId;

    /** 设备唯一编号 */
    private String deviceImei;

    /** 设备名称 */
    private String deviceName;

    /** 设备类型 */
    private Integer deviceType;

    /** 设备状态：1 启用, 0 禁用 */
    private Integer deviceStatus;

    /** 车牌 */
    private String plate;

    /** 设备颜色 */
    private String deviceColour;

    /** 头像 */
    private String headPic;

    /** 经度 */
    private BigDecimal lastLongitude;

    /** 纬度 */
    private BigDecimal lastLatitude;

    /** 最新地址详情 */
    private String lastPositionDesc;

    /** 最后一次定位时间 */
    private LocalDateTime lastLocationTime;

    /** 电量 */
    private String lastDeviceVol;

    /** 信号强度 */
    private String lastDeviceSms;

    /** 速度 */
    private BigDecimal lastSpeed;

    /** 设备状态：0 静止, 1 运动 */
    private Integer lastMotionStatus;

    /** 有效期 */
    private LocalDate expireDate;

    /** 当前用户是否为主控：0 一般用户, 1 主控 */
    private Integer mainControl;

    /** 用户与该设备的关系 */
    private String relationShip;
}
