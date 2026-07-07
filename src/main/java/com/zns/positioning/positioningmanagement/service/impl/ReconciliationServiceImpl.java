package com.zns.positioning.positioningmanagement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zns.positioning.positioningmanagement.common.enums.DiffTypeEnum;
import com.zns.positioning.positioningmanagement.dto.ReconciliationDetailQueryDTO;
import com.zns.positioning.positioningmanagement.dto.ReconciliationGenerateDTO;
import com.zns.positioning.positioningmanagement.dto.ReconciliationQueryDTO;
import com.zns.positioning.positioningmanagement.entity.RechargeOrder;
import com.zns.positioning.positioningmanagement.entity.ReconciliationDetail;
import com.zns.positioning.positioningmanagement.entity.ReconciliationReport;
import com.zns.positioning.positioningmanagement.mapper.RechargeOrderMapper;
import com.zns.positioning.positioningmanagement.mapper.ReconciliationDetailMapper;
import com.zns.positioning.positioningmanagement.mapper.ReconciliationReportMapper;
import com.zns.positioning.positioningmanagement.service.ReconciliationService;
import com.zns.positioning.positioningmanagement.vo.ReconciliationDetailVO;
import com.zns.positioning.positioningmanagement.vo.ReconciliationReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationServiceImpl implements ReconciliationService {

    private final ReconciliationReportMapper reconciliationReportMapper;
    private final ReconciliationDetailMapper reconciliationDetailMapper;
    private final RechargeOrderMapper rechargeOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReconciliationReportVO generateReport(ReconciliationGenerateDTO dto, String operator) {
        LocalDate reportDate = dto.getReportDate();
        LocalDateTime startTime = reportDate.atStartOfDay();
        LocalDateTime endTime = reportDate.plusDays(1).atStartOfDay();

        // 删除已有报告 (如果存在)
        List<ReconciliationReport> existingReports = reconciliationReportMapper.selectList(
                new LambdaQueryWrapper<ReconciliationReport>()
                        .eq(ReconciliationReport::getReportDate, reportDate));
        for (ReconciliationReport r : existingReports) {
            reconciliationDetailMapper.delete(
                    new LambdaQueryWrapper<ReconciliationDetail>()
                            .eq(ReconciliationDetail::getReportId, r.getId()));
            reconciliationReportMapper.deleteById(r.getId());
        }

        // 创建对账报告
        ReconciliationReport report = new ReconciliationReport();
        report.setReportNo("REC" + reportDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + IdUtil.fastSimpleUUID().substring(0, 6));
        report.setReportDate(reportDate);
        report.setStartTime(startTime);
        report.setEndTime(endTime);
        report.setOperator(operator);
        reconciliationReportMapper.insert(report);

        // 获取时间段内成功的充值订单
        List<RechargeOrder> orders = rechargeOrderMapper.selectSuccessOrdersBetween(startTime, endTime);
        int matchCount = 0;
        int diffCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal diffAmount = BigDecimal.ZERO;

        for (RechargeOrder order : orders) {
            ReconciliationDetail detail = new ReconciliationDetail();
            detail.setReportId(report.getId());
            detail.setOrderNo(order.getOrderNo());
            detail.setTradeTime(order.getRechargeTime());
            detail.setAmount(order.getAmount());
            detail.setOurStatus("SUCCESS");
            totalAmount = totalAmount.add(order.getAmount());

            // 模拟运营商侧数据对比
            String extOrderNo = "EXT_" + order.getOrderNo();
            detail.setExternalOrderNo(extOrderNo);
            // 模拟：90%概率匹配，10%概率差异
            if (Math.random() > 0.1) {
                detail.setExternalStatus("SUCCESS");
                detail.setDiffType(DiffTypeEnum.NO_DIFF.name());
                matchCount++;
            } else {
                detail.setExternalStatus("NOT_FOUND");
                detail.setDiffType(DiffTypeEnum.MISSING_EXTERNAL.name());
                detail.setDiffDesc("运营商侧未找到该交易记录");
                diffCount++;
                diffAmount = diffAmount.add(order.getAmount());
            }
            reconciliationDetailMapper.insert(detail);
        }

        report.setTotalCount(orders.size());
        report.setMatchCount(matchCount);
        report.setDiffCount(diffCount);
        report.setTotalAmount(totalAmount);
        report.setDiffAmount(diffAmount);
        report.setStatus(diffCount > 0 ? "PENDING" : "RESOLVED");
        reconciliationReportMapper.updateById(report);

        ReconciliationReportVO vo = new ReconciliationReportVO();
        BeanUtil.copyProperties(report, vo);
        return vo;
    }

    @Override
    public Page<ReconciliationReportVO> queryReports(ReconciliationQueryDTO dto) {
        LambdaQueryWrapper<ReconciliationReport> wrapper = new LambdaQueryWrapper<>();
        if (dto.getReportDateStart() != null) {
            wrapper.ge(ReconciliationReport::getReportDate, dto.getReportDateStart());
        }
        if (dto.getReportDateEnd() != null) {
            wrapper.le(ReconciliationReport::getReportDate, dto.getReportDateEnd());
        }
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            wrapper.eq(ReconciliationReport::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(ReconciliationReport::getCreateTime);

        Page<ReconciliationReport> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<ReconciliationReport> result = reconciliationReportMapper.selectPage(page, wrapper);

        Page<ReconciliationReportVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ReconciliationReportVO> voList = new ArrayList<>();
        for (ReconciliationReport report : result.getRecords()) {
            ReconciliationReportVO vo = new ReconciliationReportVO();
            BeanUtil.copyProperties(report, vo);
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<ReconciliationDetailVO> queryReportDetails(Long reportId, ReconciliationDetailQueryDTO dto) {
        LambdaQueryWrapper<ReconciliationDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReconciliationDetail::getReportId, reportId);
        if (dto.getDiffType() != null && !dto.getDiffType().isEmpty()) {
            wrapper.eq(ReconciliationDetail::getDiffType, dto.getDiffType());
        }
        if (dto.getProcessStatus() != null && !dto.getProcessStatus().isEmpty()) {
            wrapper.eq(ReconciliationDetail::getProcessStatus, dto.getProcessStatus());
        }
        wrapper.orderByAsc(ReconciliationDetail::getId);

        Page<ReconciliationDetail> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<ReconciliationDetail> result = reconciliationDetailMapper.selectPage(page, wrapper);

        Page<ReconciliationDetailVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ReconciliationDetailVO> voList = new ArrayList<>();
        for (ReconciliationDetail detail : result.getRecords()) {
            ReconciliationDetailVO vo = new ReconciliationDetailVO();
            BeanUtil.copyProperties(detail, vo);
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void markDetailProcessStatus(Long detailId, String processStatus, String processRemark, String operator) {
        ReconciliationDetail detail = reconciliationDetailMapper.selectById(detailId);
        if (detail == null) {
            throw new RuntimeException("对账明细不存在");
        }
        detail.setProcessStatus(processStatus);
        detail.setProcessRemark(processRemark + " (操作人: " + operator + ")");
        reconciliationDetailMapper.updateById(detail);

        // 检查报告下所有差异是否都已处理
        List<ReconciliationDetail> diffs = reconciliationDetailMapper.selectDiffsByReportId(detail.getReportId());
        boolean allResolved = diffs.stream()
                .allMatch(d -> "RESOLVED".equals(d.getProcessStatus()) || "IGNORED".equals(d.getProcessStatus()) || d.getId().equals(detailId));
        if (allResolved) {
            ReconciliationReport report = reconciliationReportMapper.selectById(detail.getReportId());
            if (report != null && !"CLOSED".equals(report.getStatus())) {
                report.setStatus("RESOLVED");
                reconciliationReportMapper.updateById(report);
            }
        }
    }

    @Override
    public void resolveReport(Long reportId, String operator) {
        ReconciliationReport report = reconciliationReportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("报告不存在");
        }
        report.setStatus("RESOLVED");
        report.setOperator(operator);
        reconciliationReportMapper.updateById(report);
    }

    @Override
    public ReconciliationReportVO regenerateReport(LocalDate reportDate, String operator) {
        ReconciliationGenerateDTO dto = new ReconciliationGenerateDTO();
        dto.setReportDate(reportDate);
        return generateReport(dto, operator);
    }
}
