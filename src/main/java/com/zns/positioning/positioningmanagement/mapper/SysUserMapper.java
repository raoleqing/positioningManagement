package com.zns.positioning.positioningmanagement.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zns.positioning.positioningmanagement.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper（指向 slave 数据源 117.50.157.179）
 */
@DS("slave")
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
