package com.zns.positioning.positioningmanagement.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.common.R;
import com.zns.positioning.positioningmanagement.service.OperationLogService;
import com.zns.positioning.positioningmanagement.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志管理 Controller
 */
@RestController
@RequestMapping("/admin/operation-log")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     *
     * @param pageNum     页码
     * @param pageSize    每页大小
     * @param businessNo  业务编号（订单号），模糊搜索
     * @param businessType 业务类型
     * @param logLevel    日志级别
     * @param startTime   开始时间
     * @param endTime     结束时间
     */
    @GetMapping("/list")
    public R<Page<OperationLogVO>> list(@RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "20") int pageSize,
                                         @RequestParam(required = false) String businessNo,
                                         @RequestParam(required = false) String businessType,
                                         @RequestParam(required = false) String logLevel,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<OperationLogVO> page = new Page<>(pageNum, pageSize);
        Page<OperationLogVO> result = operationLogService.queryPage(page, businessNo, businessType, logLevel, startTime, endTime);
        return R.ok(result);
    }

    /**
     * 根据订单号追踪完整链路（支付→充值→通知）
     *
     * @param businessNo 业务编号（订单号）
     */
    @GetMapping("/trace/{businessNo}")
    public R<List<OperationLogVO>> trace(@PathVariable String businessNo) {
        List<OperationLogVO> logs = operationLogService.queryByBusinessNo(businessNo);
        return R.ok(logs);
    }
}
