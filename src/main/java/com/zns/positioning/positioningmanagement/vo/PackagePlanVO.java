package com.zns.positioning.positioningmanagement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 套餐信息 VO
 */
@Data
public class PackagePlanVO {

    private Long id;

    /** 套餐名称 */
    private String planName;

    /** 年数 */
    private Integer years;

    /** 价格(元) */
    private BigDecimal price;

    /** 状态: 0-停用, 1-启用 */
    private Integer status;

    /** 状态文字 */
    private String statusText;

    /** 排序 */
    private Integer sortOrder;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
