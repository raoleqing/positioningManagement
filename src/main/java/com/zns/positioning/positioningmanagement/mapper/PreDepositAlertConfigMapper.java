package com.zns.positioning.positioningmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zns.positioning.positioningmanagement.entity.PreDepositAlertConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PreDepositAlertConfigMapper extends BaseMapper<PreDepositAlertConfig> {

    @Select("SELECT * FROM pre_deposit_alert_config WHERE account_id = #{accountId}")
    PreDepositAlertConfig selectByAccountId(@Param("accountId") Long accountId);
}
