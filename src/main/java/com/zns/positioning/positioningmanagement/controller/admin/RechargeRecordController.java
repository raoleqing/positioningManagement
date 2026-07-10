package com.zns.positioning.positioningmanagement.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.common.R;
import com.zns.positioning.positioningmanagement.entity.RechargeRecord;
import com.zns.positioning.positioningmanagement.service.RechargeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 预存款消耗明细（充值记录）
 */
@RestController
@RequestMapping("/recharge-record")
@RequiredArgsConstructor
public class RechargeRecordController {

    private final RechargeRecordService rechargeRecordService;

    /** 分页查询充值记录 */
    @GetMapping("/list")
    public R<Page<RechargeRecord>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String rechargeStatus,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return R.ok(rechargeRecordService.queryPage(pageNum, pageSize,
                orderNo, rechargeStatus, startTime, endTime));
    }

    /** 重试充值 */
    @PostMapping("/{id}/retry")
    public R<Void> retry(@PathVariable Long id, @RequestParam(defaultValue = "admin") String operator) {
        rechargeRecordService.retryRecharge(id, operator);
        return R.ok();
    }

    /** 退款 */
    @PostMapping("/{id}/refund")
    public R<Void> refund(@PathVariable Long id, @RequestParam(defaultValue = "admin") String operator) {
        rechargeRecordService.refund(id, operator);
        return R.ok();
    }
}
