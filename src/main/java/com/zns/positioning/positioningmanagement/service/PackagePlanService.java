package com.zns.positioning.positioningmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.dto.PackagePlanDTO;
import com.zns.positioning.positioningmanagement.vo.OperationLogVO;
import com.zns.positioning.positioningmanagement.vo.PackagePlanVO;

import java.util.List;

/**
 * 套餐管理 Service
 */
public interface PackagePlanService {

    /** 分页查询套餐列表（仅返回未删除的） */
    Page<PackagePlanVO> page(Integer pageNum, Integer pageSize, String planName, Integer status);

    /** 查询所有启用的套餐（供小程序端选择） */
    List<PackagePlanVO> listEnabled();

    /** 新增套餐 */
    void create(PackagePlanDTO dto, String operator);

    /** 修改套餐 */
    void update(Long id, PackagePlanDTO dto, String operator);

    /** 删除套餐（逻辑删除） */
    void delete(Long id, String operator);

    /** 修改套餐状态 */
    void updateStatus(Long id, Integer status, String operator);

    /** 查看套餐操作日志 */
    List<OperationLogVO> getLogs(Long planId);
}
