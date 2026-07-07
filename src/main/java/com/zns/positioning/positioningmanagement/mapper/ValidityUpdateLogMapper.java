package com.zns.positioning.positioningmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zns.positioning.positioningmanagement.entity.ValidityUpdateLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ValidityUpdateLogMapper extends BaseMapper<ValidityUpdateLog> {

    @Select("SELECT * FROM validity_update_log WHERE device_id = #{deviceId} ORDER BY create_time DESC")
    List<ValidityUpdateLog> selectByDeviceId(@Param("deviceId") Long deviceId);
}
