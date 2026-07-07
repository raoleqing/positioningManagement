package com.zns.positioning.positioningmanagement.controller.admin;

import com.zns.positioning.positioningmanagement.common.R;
import com.zns.positioning.positioningmanagement.dto.PreDepositAlertConfigDTO;
import com.zns.positioning.positioningmanagement.service.PreDepositService;
import com.zns.positioning.positioningmanagement.vo.PreDepositAccountVO;
import com.zns.positioning.positioningmanagement.vo.PreDepositAlertConfigVO;
import com.zns.positioning.positioningmanagement.vo.PreDepositRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/predeposit")
@RequiredArgsConstructor
public class PreDepositController {

    private final PreDepositService preDepositService;

    /** 获取预存款账户信息 */
    @GetMapping("/account")
    public R<PreDepositAccountVO> getAccount() {
        return R.ok(preDepositService.getAccount());
    }

    /** 获取预存款消耗明细 */
    @GetMapping("/records")
    public R<List<PreDepositRecordVO>> getRecords(@RequestParam(defaultValue = "1") Long accountId,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "20") Integer pageSize) {
        return R.ok(preDepositService.getRecords(accountId, pageNum, pageSize));
    }

    /** 获取告警配置 */
    @GetMapping("/alert-config/{accountId}")
    public R<PreDepositAlertConfigVO> getAlertConfig(@PathVariable Long accountId) {
        return R.ok(preDepositService.getAlertConfig(accountId));
    }

    /** 更新告警配置 */
    @PutMapping("/alert-config")
    public R<Void> updateAlertConfig(@Valid @RequestBody PreDepositAlertConfigDTO dto) {
        preDepositService.updateAlertConfig(dto);
        return R.ok();
    }
}
