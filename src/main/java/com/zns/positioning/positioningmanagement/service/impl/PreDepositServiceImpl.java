package com.zns.positioning.positioningmanagement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.zns.positioning.positioningmanagement.dto.PreDepositAlertConfigDTO;
import com.zns.positioning.positioningmanagement.entity.PreDepositAccount;
import com.zns.positioning.positioningmanagement.entity.PreDepositAlertConfig;
import com.zns.positioning.positioningmanagement.entity.PreDepositRecord;
import com.zns.positioning.positioningmanagement.mapper.PreDepositAccountMapper;
import com.zns.positioning.positioningmanagement.mapper.PreDepositAlertConfigMapper;
import com.zns.positioning.positioningmanagement.mapper.PreDepositRecordMapper;
import com.zns.positioning.positioningmanagement.service.PreDepositService;
import com.zns.positioning.positioningmanagement.vo.PreDepositAccountVO;
import com.zns.positioning.positioningmanagement.vo.PreDepositAlertConfigVO;
import com.zns.positioning.positioningmanagement.vo.PreDepositRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PreDepositServiceImpl implements PreDepositService {

    private final PreDepositAccountMapper preDepositAccountMapper;
    private final PreDepositRecordMapper preDepositRecordMapper;
    private final PreDepositAlertConfigMapper preDepositAlertConfigMapper;

    @Override
    public PreDepositAccountVO getAccount() {
        PreDepositAccount account = preDepositAccountMapper.selectById(1);
        if (account == null) {
            return null;
        }
        PreDepositAccountVO vo = new PreDepositAccountVO();
        BeanUtil.copyProperties(account, vo);
        return vo;
    }

    @Override
    public List<PreDepositRecordVO> getRecords(Long accountId, Integer pageNum, Integer pageSize) {
        List<PreDepositRecord> records = preDepositRecordMapper.selectByAccountId(accountId);
        List<PreDepositRecordVO> voList = new ArrayList<>();
        if (records != null) {
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, records.size());
            for (int i = start; i < end; i++) {
                PreDepositRecordVO vo = new PreDepositRecordVO();
                BeanUtil.copyProperties(records.get(i), vo);
                voList.add(vo);
            }
        }
        return voList;
    }

    @Override
    public PreDepositAlertConfigVO getAlertConfig(Long accountId) {
        PreDepositAlertConfig config = preDepositAlertConfigMapper.selectByAccountId(accountId);
        if (config == null) {
            return null;
        }
        PreDepositAlertConfigVO vo = new PreDepositAlertConfigVO();
        BeanUtil.copyProperties(config, vo);
        return vo;
    }

    @Override
    public void updateAlertConfig(PreDepositAlertConfigDTO dto) {
        PreDepositAlertConfig config = preDepositAlertConfigMapper.selectByAccountId(dto.getAccountId());
        if (config == null) {
            config = new PreDepositAlertConfig();
            config.setAccountId(dto.getAccountId());
        }
        config.setAlertThreshold(dto.getAlertThreshold());
        config.setAlertEnabled(dto.getAlertEnabled());
        config.setAlertPhone(dto.getAlertPhone());
        config.setAlertEmail(dto.getAlertEmail());

        if (config.getId() != null) {
            preDepositAlertConfigMapper.updateById(config);
        } else {
            preDepositAlertConfigMapper.insert(config);
        }
    }
}
