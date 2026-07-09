package com.zns.positioning.positioningmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 套餐新增/修改 DTO
 */
@Data
public class PackagePlanDTO {

    /** 套餐名称 */
    @NotBlank(message = "套餐名称不能为空")
    private String planName;

    /** 年数 */
    @NotNull(message = "年数不能为空")
    @Min(value = 1, message = "年数至少为1")
    private Integer years;

    /** 价格(元) */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    /** 状态: 0-停用, 1-启用 */
    private Integer status;

    /** 排序 */
    private Integer sortOrder;

    /** 备注 */
    private String remark;
}
