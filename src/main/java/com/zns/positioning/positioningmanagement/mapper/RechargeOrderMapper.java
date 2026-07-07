package com.zns.positioning.positioningmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.entity.RechargeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RechargeOrderMapper extends BaseMapper<RechargeOrder> {

    @Select("""
        <script>
        SELECT * FROM recharge_order WHERE deleted = 0
        <if test='deviceId != null'> AND device_id = #{deviceId} </if>
        <if test='userId != null'> AND user_id = #{userId} </if>
        <if test='startTime != null'> AND create_time &gt;= #{startTime} </if>
        <if test='endTime != null'> AND create_time &lt;= #{endTime} </if>
        <if test='orderNo != null and orderNo != \"\"'> AND order_no = #{orderNo} </if>
        <if test='payStatus != null and payStatus != \"\"'> AND pay_status = #{payStatus} </if>
        <if test='rechargeStatus != null and rechargeStatus != \"\"'> AND recharge_status = #{rechargeStatus} </if>
        ORDER BY create_time DESC
        </script>
        """)
    IPage<RechargeOrder> selectPageByCondition(Page<RechargeOrder> page,
                                                @Param("deviceId") Long deviceId,
                                                @Param("userId") Long userId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("orderNo") String orderNo,
                                                @Param("payStatus") String payStatus,
                                                @Param("rechargeStatus") String rechargeStatus);

    @Select("SELECT * FROM recharge_order WHERE recharge_status = 'FAILED' AND retry_count < max_retry_count AND deleted = 0")
    List<RechargeOrder> selectFailedRetryableOrders();

    @Select("SELECT SUM(amount) FROM recharge_order WHERE recharge_status = 'SUCCESS' AND recharge_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0")
    BigDecimal sumSuccessAmountBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM recharge_order WHERE recharge_status = 'SUCCESS' AND recharge_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 ORDER BY recharge_time ASC")
    List<RechargeOrder> selectSuccessOrdersBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
