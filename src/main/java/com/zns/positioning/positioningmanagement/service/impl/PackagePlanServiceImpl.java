package com.zns.positioning.positioningmanagement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.common.enums.OperationLogTypeEnum;
import com.zns.positioning.positioningmanagement.dto.PackagePlanDTO;
import com.zns.positioning.positioningmanagement.entity.PackagePlan;
import com.zns.positioning.positioningmanagement.mapper.PackagePlanMapper;
import com.zns.positioning.positioningmanagement.service.OperationLogService;
import com.zns.positioning.positioningmanagement.service.PackagePlanService;
import com.zns.positioning.positioningmanagement.vo.OperationLogVO;
import com.zns.positioning.positioningmanagement.vo.PackagePlanVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 套餐管理 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PackagePlanServiceImpl implements PackagePlanService {

    private final PackagePlanMapper packagePlanMapper;
    private final OperationLogService operationLogService;

    @Override
    public Page<PackagePlanVO> page(Integer pageNum, Integer pageSize, String planName, Integer status) {
        LambdaQueryWrapper<PackagePlan> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(planName)) {
            wrapper.like(PackagePlan::getPlanName, planName);
        }
        if (status != null) {
            wrapper.eq(PackagePlan::getStatus, status);
        }
        wrapper.orderByAsc(PackagePlan::getSortOrder).orderByDesc(PackagePlan::getCreateTime);

        Page<PackagePlan> page = packagePlanMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<PackagePlanVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<PackagePlanVO> voList = new ArrayList<>();
        for (PackagePlan plan : page.getRecords()) {
            voList.add(toVO(plan));
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<PackagePlanVO> listEnabled() {
        LambdaQueryWrapper<PackagePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PackagePlan::getStatus, 1)
                .orderByAsc(PackagePlan::getSortOrder)
                .orderByDesc(PackagePlan::getCreateTime);
        List<PackagePlan> list = packagePlanMapper.selectList(wrapper);
        List<PackagePlanVO> voList = new ArrayList<>();
        for (PackagePlan plan : list) {
            voList.add(toVO(plan));
        }
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(PackagePlanDTO dto, String operator) {
        PackagePlan plan = new PackagePlan();
        BeanUtil.copyProperties(dto, plan);
        if (plan.getStatus() == null) {
            plan.setStatus(1);
        }
        if (plan.getSortOrder() == null) {
            plan.setSortOrder(0);
        }
        packagePlanMapper.insert(plan);

        // 记录操作日志
        operationLogService.saveLog(OperationLogTypeEnum.PACKAGE_CREATE,
                plan.getId(), null, plan.getPlanName(),
                "INFO", "新增套餐", JSONUtil.toJsonStr(plan), operator);

        log.info("新增套餐成功, id={}, name={}, operator={}", plan.getId(), plan.getPlanName(), operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PackagePlanDTO dto, String operator) {
        PackagePlan plan = packagePlanMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }

        // 记录变更前的数据，用于日志
        Map<String, Object> beforeMap = BeanUtil.beanToMap(plan, false, true);

        BeanUtil.copyProperties(dto, plan);
        plan.setId(id);
        packagePlanMapper.updateById(plan);

        // 记录变更详情
        Map<String, Object> afterMap = BeanUtil.beanToMap(plan, false, true);
        Map<String, Object> changeDetail = new HashMap<>();
        changeDetail.put("before", beforeMap);
        changeDetail.put("after", afterMap);

        operationLogService.saveLog(OperationLogTypeEnum.PACKAGE_UPDATE,
                plan.getId(), null, plan.getPlanName(),
                "INFO", "修改套餐", JSONUtil.toJsonStr(changeDetail), operator);

        log.info("修改套餐成功, id={}, name={}, operator={}", plan.getId(), plan.getPlanName(), operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, String operator) {
        PackagePlan plan = packagePlanMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }

        packagePlanMapper.deleteById(id);

        operationLogService.saveLog(OperationLogTypeEnum.PACKAGE_DELETE,
                plan.getId(), null, plan.getPlanName(),
                "WARN", "删除套餐", JSONUtil.toJsonStr(plan), operator);

        log.info("删除套餐成功, id={}, name={}, operator={}", plan.getId(), plan.getPlanName(), operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status, String operator) {
        PackagePlan plan = packagePlanMapper.selectById(id);
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }

        Integer oldStatus = plan.getStatus();
        plan.setStatus(status);
        packagePlanMapper.updateById(plan);

        Map<String, Object> detail = new HashMap<>();
        detail.put("oldStatus", oldStatus == 1 ? "启用" : "停用");
        detail.put("newStatus", status == 1 ? "启用" : "停用");

        operationLogService.saveLog(OperationLogTypeEnum.PACKAGE_STATUS_CHANGE,
                plan.getId(), null, plan.getPlanName(),
                "INFO",
                "状态变更: " + (oldStatus == 1 ? "启用 → 停用" : "停用 → 启用"),
                JSONUtil.toJsonStr(detail), operator);

        log.info("套餐状态变更成功, id={}, name={}, status={}→{}, operator={}",
                plan.getId(), plan.getPlanName(), oldStatus, status, operator);
    }

    @Override
    public List<OperationLogVO> getLogs(Long planId) {
        return operationLogService.getLogs("PACKAGE_PLAN", planId);
    }

    // ==================== 私有方法 ====================

    private PackagePlanVO toVO(PackagePlan plan) {
        PackagePlanVO vo = new PackagePlanVO();
        BeanUtil.copyProperties(plan, vo);
        vo.setStatusText(plan.getStatus() == 1 ? "启用" : "停用");
        return vo;
    }
}
