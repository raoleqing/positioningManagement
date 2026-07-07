package com.zns.positioning.positioningmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zns.positioning.positioningmanagement.entity.DeviceValidity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper
public interface DeviceValidityMapper extends BaseMapper<DeviceValidity> {

    @Update("UPDATE device_validity SET valid_to = #{validTo}, update_time = NOW(), operator = #{operator} WHERE device_id = #{deviceId}")
    int updateValidTo(@Param("deviceId") Long deviceId, @Param("validTo") LocalDate validTo, @Param("operator") String operator);

    @Select("SELECT * FROM device_validity WHERE device_id = #{deviceId}")
    DeviceValidity selectByDeviceId(@Param("deviceId") Long deviceId);

    @Select("SELECT * FROM device_validity WHERE valid_to = #{validTo} AND status = 'EXPIRING'")
    java.util.List<DeviceValidity> selectExpiringByDate(@Param("validTo") LocalDate validTo);
}
