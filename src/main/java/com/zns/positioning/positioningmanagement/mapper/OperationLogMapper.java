package com.zns.positioning.positioningmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zns.positioning.positioningmanagement.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通用操作日志 Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
