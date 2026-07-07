package com.zns.positioning.positioningmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zns.positioning.positioningmanagement.entity.PreDepositRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PreDepositRecordMapper extends BaseMapper<PreDepositRecord> {

    @Select("SELECT * FROM pre_deposit_record WHERE account_id = #{accountId} ORDER BY create_time DESC")
    List<PreDepositRecord> selectByAccountId(@Param("accountId") Long accountId);
}
