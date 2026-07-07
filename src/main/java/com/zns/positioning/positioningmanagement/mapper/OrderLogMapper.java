package com.zns.positioning.positioningmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zns.positioning.positioningmanagement.entity.OrderLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderLogMapper extends BaseMapper<OrderLog> {

    @Select("SELECT * FROM order_log WHERE order_id = #{orderId} ORDER BY create_time ASC")
    List<OrderLog> selectByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT * FROM order_log WHERE order_no = #{orderNo} ORDER BY create_time ASC")
    List<OrderLog> selectByOrderNo(@Param("orderNo") String orderNo);
}
