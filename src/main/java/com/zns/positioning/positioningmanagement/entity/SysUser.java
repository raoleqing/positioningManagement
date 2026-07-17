package com.zns.positioning.positioningmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户（来自 slave 数据源 117.50.157.179）
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Integer userId;

    /** 部门ID */
    private Integer deptId;

    /** 用户名称 */
    private String userName;

    /** 邮箱 */
    private String email;

    /** 手机号码 */
    private String mobile;

    /** 性别：1 男, 2 女, 0 未知 */
    private Integer sex;

    /** 口令 */
    private String password;

    /** 状态：1 有效, 0 无效 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

    /** 客户ID */
    private Long customerId;

    /** 用户类型：1 平台用户, 2 客户管理员, 3 普通用户 */
    private Integer userType;

    /** 过期时间 */
    private LocalDateTime expireDate;
}
