package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备详情 VO（移动端）
 */
@Data
public class DeviceDetailVO {

    // ==================== 基本信息 ====================

    /** 设备ID */
    private Integer deviceId;

    /** 设备唯一编号（IMEI） */
    private String deviceImei;

    /** 设备名称 */
    private String deviceName;

    /** 电话 */
    private String bindMobile;

    /** 设备版本 */
    private String version;

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

    /** 车队 */
    private String organization;

    /** 短号 */
    private String deviceShortNumber;

    /** 家庭号码 */
    private String deviceFamilyNumber;

    /** 有效期 */
    private LocalDate expireDate;

    /** 备注 */
    private String deviceRemark;

    /** 设备主人ID */
    private Long deviceOwner;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ==================== 定位信息 ====================

    /** 经度 */
    private BigDecimal lastLongitude;

    /** 纬度 */
    private BigDecimal lastLatitude;

    /** 最新地址详情 */
    private String lastPositionDesc;

    /** 最后一次定位时间 */
    private LocalDateTime lastLocationTime;

    /** 定位类型：0 GPS, 1 WIFI, 2 基站 */
    private Integer lastLocationType;

    /** 速度 */
    private BigDecimal lastSpeed;

    /** 定位精度 */
    private BigDecimal accuracy;

    /** 方向角 */
    private BigDecimal heading;

    /** 海拔 */
    private BigDecimal altitude;

    // ==================== 设备状态 ====================

    /** 运动状态：0 静止, 1 运动 */
    private Integer lastMotionStatus;

    /** 电量 */
    private String lastDeviceVol;

    /** 信号强度 */
    private String lastDeviceSms;

    /** 油量 */
    private BigDecimal fuel;

    /** 里程（单位：米） */
    private BigDecimal odometer;

    /** 基础里程 */
    private Float baseMileage;

    // ==================== 配置信息 ====================

    /** 时区 */
    private Integer timeZone;

    /** 定位间隔 */
    private Integer regularTimer;

    /** 是否启用通知：0 禁用, 1 启用 */
    private Integer notifyStatus;

    // ==================== 关联信息 ====================

    /** 设备主人名称 */
    private String ownerName;

    /** 设备附加属性列表（K-V） */
    private List<Map<String, String>> additional;

    /** 绑定用户列表 */
    private List<BindUserVO> bindUsers;

    /**
     * 绑定用户信息
     */
    @Data
    public static class BindUserVO {
        /** 用户ID */
        private Integer userId;

        /** 用户名称 */
        private String userName;

        /** 手机号 */
        private String mobile;

        /** 是否主控：0 一般用户, 1 主控 */
        private Integer mainControl;

        /** 与设备拥有者的关系 */
        private String relationShip;
    }
}
