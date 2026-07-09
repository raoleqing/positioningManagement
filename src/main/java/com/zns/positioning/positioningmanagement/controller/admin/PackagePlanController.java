package com.zns.positioning.positioningmanagement.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.common.R;
import com.zns.positioning.positioningmanagement.dto.PackagePlanDTO;
import com.zns.positioning.positioningmanagement.service.PackagePlanService;
import com.zns.positioning.positioningmanagement.vo.OperationLogVO;
import com.zns.positioning.positioningmanagement.vo.PackagePlanVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理 Controller
 */
@RestController
@RequestMapping("/package-plan")
@RequiredArgsConstructor
public class PackagePlanController {

    private final PackagePlanService packagePlanService;

    /** 分页查询套餐列表 */
    @GetMapping("/list")
    public R<Page<PackagePlanVO>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "20") Integer pageSize,
                                        @RequestParam(required = false) String planName,
                                        @RequestParam(required = false) Integer status) {
        return R.ok(packagePlanService.page(pageNum, pageSize, planName, status));
    }

    /** 新增套餐 */
    @PostMapping
    public R<Void> create(@Valid @RequestBody PackagePlanDTO dto,
                           @RequestParam String operator) {
        packagePlanService.create(dto, operator);
        return R.ok();
    }

    /** 修改套餐 */
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id,
                           @Valid @RequestBody PackagePlanDTO dto,
                           @RequestParam String operator) {
        packagePlanService.update(id, dto, operator);
        return R.ok();
    }

    /** 删除套餐（逻辑删除） */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id,
                           @RequestParam String operator) {
        packagePlanService.delete(id, operator);
        return R.ok();
    }

    /** 修改套餐状态 */
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id,
                                 @RequestParam Integer status,
                                 @RequestParam String operator) {
        packagePlanService.updateStatus(id, status, operator);
        return R.ok();
    }

    /** 查看套餐操作日志 */
    @GetMapping("/{planId}/logs")
    public R<List<OperationLogVO>> getLogs(@PathVariable Long planId) {
        return R.ok(packagePlanService.getLogs(planId));
    }
}
