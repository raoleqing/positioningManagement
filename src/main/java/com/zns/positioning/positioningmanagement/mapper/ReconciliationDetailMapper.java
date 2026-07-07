package com.zns.positioning.positioningmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zns.positioning.positioningmanagement.entity.ReconciliationDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReconciliationDetailMapper extends BaseMapper<ReconciliationDetail> {

    @Select("SELECT * FROM reconciliation_detail WHERE report_id = #{reportId} ORDER BY id ASC")
    List<ReconciliationDetail> selectByReportId(@Param("reportId") Long reportId);

    @Select("SELECT * FROM reconciliation_detail WHERE report_id = #{reportId} AND diff_type != 'NO_DIFF'")
    List<ReconciliationDetail> selectDiffsByReportId(@Param("reportId") Long reportId);
}
