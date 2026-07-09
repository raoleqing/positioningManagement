package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 套餐管理实体
 */
@Data
@TableName("package_plan")
public class PackagePlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 套餐名称 */
    private String planName;

    /** 年数 */
    private Integer years;

    /** 价格(元) */
    private BigDecimal price;

    /** 状态: 0-停用, 1-启用 */
    private Integer status;

    /** 排序 */
    private Integer sortOrder;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除: 0-未删除, 1-已删除 */
    @TableLogic
    private Integer deleted;
}
