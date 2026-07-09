package com.zns.positioning.positioningmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 小程序端-创建充值订单请求
 */
@Data
public class CreateOrderDTO {

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String userName;

    /** 设备ID */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /** 设备编号 */
    @NotBlank(message = "设备编号不能为空")
    private String deviceNo;

    /** SIM卡号 */
    private String simCardNo;

    /** ICCID（SIM卡唯一标识，运营商充值必传） */
    @NotBlank(message = "ICCID不能为空")
    private String iccid;

    /** 套餐ID */
    @NotNull(message = "套餐ID不能为空")
    private Long planId;

    /** 备注 */
    private String remark;
}
