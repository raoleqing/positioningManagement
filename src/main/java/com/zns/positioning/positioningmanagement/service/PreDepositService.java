package com.zns.positioning.positioningmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.dto.PreDepositAlertConfigDTO;
import com.zns.positioning.positioningmanagement.vo.PreDepositAccountVO;
import com.zns.positioning.positioningmanagement.vo.PreDepositAlertConfigVO;
import com.zns.positioning.positioningmanagement.vo.PreDepositRecordVO;

import java.util.List;

public interface PreDepositService {

    /** 获取预存款账户信息 */
    PreDepositAccountVO getAccount();

    /** 获取预存款消耗明细 */
    List<PreDepositRecordVO> getRecords(Long accountId, Integer pageNum, Integer pageSize);

    /** 获取告警配置 */
    PreDepositAlertConfigVO getAlertConfig(Long accountId);

    /** 更新告警配置 */
    void updateAlertConfig(PreDepositAlertConfigDTO dto);
}
