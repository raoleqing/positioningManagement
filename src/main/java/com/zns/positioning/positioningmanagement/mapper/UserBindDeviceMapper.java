package com.zns.positioning.positioningmanagement.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zns.positioning.positioningmanagement.entity.UserBindDevice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户设备绑定关系 Mapper（指向 slave 数据源 117.50.157.179）
 */
@DS("slave")
@Mapper
public interface UserBindDeviceMapper extends BaseMapper<UserBindDevice> {
}
