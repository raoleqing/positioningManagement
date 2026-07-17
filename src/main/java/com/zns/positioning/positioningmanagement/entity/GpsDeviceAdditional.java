package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备信息附加表（来自 slave 数据源 117.50.157.179）
 * 联合唯一键：device_id + doc_key
 */
@Data
@TableName("gps_device_additional")
public class GpsDeviceAdditional {

    /** 设备ID */
    private Integer deviceId;

    /** 属性键 */
    private String docKey;

    /** 属性值 */
    private String docValue;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
