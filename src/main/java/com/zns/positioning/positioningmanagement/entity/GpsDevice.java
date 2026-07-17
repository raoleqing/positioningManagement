package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备信息（来自 slave 数据源）
 */
@Data
@TableName("gps_device")
public class GpsDevice {

    @TableId(type = IdType.AUTO)
    private Integer deviceId;

    /** 设备唯一编号 */
    private String deviceImei;

    /** 设备名称 */
    private String deviceName;

    /** 电话 */
    private String bindMobile;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 设备版本 */
    private String version;

    /** 修改时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 头像 */
    private String headPic;

    /** 设备类型（代码 DEVICE_TYPE） */
    private Integer deviceType;

    /** 经度 */
    private BigDecimal lastLongitude;

    /** 纬度 */
    private BigDecimal lastLatitude;

    /** 最后一次定位时间 */
    private LocalDateTime lastLocationTime;

    /** 定位类型：0 GPS, 1 WIFI, 2 基站 */
    private Integer lastLocationType;

    /** 速度 */
    private BigDecimal lastSpeed;

    /** 设备状态：0 静止, 1 运动 */
    private Integer lastMotionStatus;

    /** 最新地址详情 */
    private String lastPositionDesc;

    /** 电量 */
    private String lastDeviceVol;

    /** 信号强度 */
    private String lastDeviceSms;

    /** 短号 */
    private String deviceShortNumber;

    /** 家庭号码 */
    private String deviceFamilyNumber;

    /** 有效期 */
    private LocalDate expireDate;

    /** 备注 */
    private String deviceRemark;

    /** 车队 */
    private String organization;

    /** 里程（单位：米） */
    private BigDecimal odometer;

    /** 方向角 */
    private BigDecimal heading;

    /** 客户ID */
    private Long customerId;

    /** 用户ID */
    private Long userId;

    /** 部门ID */
    private Long deptId;

    /** 车牌 */
    private String plate;

    /** 时区 */
    private Integer timeZone;

    /** 基础里程 */
    private Float baseMileage;

    /** 颜色 */
    private String deviceColour;

    /** 设备状态：1 启用, 0 禁用 */
    private Integer deviceStatus;

    /** 油量 */
    private BigDecimal fuel;

    /** 海拔 */
    private BigDecimal altitude;

    /**
     * 最新位置（MySQL POINT 空间类型）
     * 读取时为 byte[]（WKB格式），写入需用 ST_GeomFromText 自定义SQL
     */
    private Object lastPosition;

    /** 平时设备定位间隔 */
    private Integer regularTimer;

    /** 是否启用通知：0 禁用, 1 启用 */
    private Integer notifyStatus;

    /** 定位精度 */
    private BigDecimal accuracy;

    /** 设备主人 */
    private Long deviceOwner;
}
