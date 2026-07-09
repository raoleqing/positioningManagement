package com.zns.positioning.positioningmanagement.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.common.R;
import com.zns.positioning.positioningmanagement.dto.OrderQueryDTO;
import com.zns.positioning.positioningmanagement.service.RechargeOrderService;
import com.zns.positioning.positioningmanagement.vo.OperationLogVO;
import com.zns.positioning.positioningmanagement.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class RechargeOrderController {

    private final RechargeOrderService rechargeOrderService;

    /** 分页查询订单列表 */
    @GetMapping("/list")
    public R<Page<OrderVO>> list(OrderQueryDTO dto) {
        return R.ok(rechargeOrderService.queryOrders(dto));
    }

    /** 查看订单详细日志 */
    @GetMapping("/{orderId}/logs")
    public R<List<OperationLogVO>> getOrderLogs(@PathVariable Long orderId) {
        return R.ok(rechargeOrderService.getOrderLogs(orderId));
    }

    /** 手动重试单个失败订单 */
    @PostMapping("/{orderId}/retry")
    public R<Void> retryOrder(@PathVariable Long orderId, @RequestParam String operator) {
        rechargeOrderService.retryOrder(orderId, operator);
        return R.ok();
    }

    /** 一键重试所有失败订单 */
    @PostMapping("/retry-all")
    public R<Integer> retryAll(@RequestParam String operator) {
        int count = rechargeOrderService.retryAllFailedOrders(operator);
        return R.ok("成功重试 " + count + " 笔订单", count);
    }
}
