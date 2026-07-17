package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户设备绑定关系（来自 slave 数据源 117.50.157.179）
 */
@Data
@TableName("user_bind_device")
public class UserBindDevice {

    @TableId(type = IdType.AUTO)
    private Integer bindId;

    /** 设备ID */
    private Integer deviceId;

    /** 用户ID */
    private Integer userId;

    /** 主控标识：0 一般用户, 1 主控 */
    private Integer mainControl;

    /** 备注 */
    private String remark;

    /** 绑定时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 客户ID */
    private Long customerId;

    /** 与设备拥有者的关系 */
    private String relationShip;

    /** 附加说明 */
    private String additionalNotes;
}
