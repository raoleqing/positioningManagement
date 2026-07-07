package com.zns.positioning.positioningmanagement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.zns.positioning.positioningmanagement.common.enums.NotifyStatusEnum;
import com.zns.positioning.positioningmanagement.common.enums.OrderLogTypeEnum;
import com.zns.positioning.positioningmanagement.common.enums.PayStatusEnum;
import com.zns.positioning.positioningmanagement.common.enums.RechargeStatusEnum;
import com.zns.positioning.positioningmanagement.config.WechatPayConfig;
import com.zns.positioning.positioningmanagement.dto.CreateOrderDTO;
import com.zns.positioning.positioningmanagement.dto.MobileOrderQueryDTO;
import com.zns.positioning.positioningmanagement.entity.*;
import com.zns.positioning.positioningmanagement.mapper.*;
import com.zns.positioning.positioningmanagement.service.MobileRechargeService;
import com.zns.positioning.positioningmanagement.vo.DeviceSimplifyVO;
import com.zns.positioning.positioningmanagement.vo.DeviceValidityVO;
import com.zns.positioning.positioningmanagement.vo.MobileOrderDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobileRechargeServiceImpl implements MobileRechargeService {

    private final RechargeOrderMapper rechargeOrderMapper;
    private final OrderLogMapper orderLogMapper;
    private final DeviceValidityMapper deviceValidityMapper;
    private final PreDepositAccountMapper preDepositAccountMapper;
    private final PreDepositRecordMapper preDepositRecordMapper;
    private final WechatPayConfig wechatPayConfig;

    private final Config wechatPaySdkConfig;
    private JsapiService jsapiService;
    private JsapiServiceExtension jsapiServiceExtension;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MobileOrderDetailVO createOrder(CreateOrderDTO dto) {
        // 生成订单号：REC + 年月日 + 6位随机数
        String orderNo = "REC" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss")
                + IdUtil.fastSimpleUUID().substring(0, 6);

        RechargeOrder order = new RechargeOrder();
        order.setOrderNo(orderNo);
        order.setUserId(dto.getUserId());
        order.setUserName(dto.getUserName());
        order.setDeviceId(dto.getDeviceId());
        order.setDeviceNo(dto.getDeviceNo());
        order.setSimCardNo(dto.getSimCardNo());
        order.setAmount(dto.getAmount());
        order.setYears(dto.getYears());
        order.setPayStatus(PayStatusEnum.PENDING.name());
        order.setRechargeStatus(RechargeStatusEnum.PENDING.name());
        order.setNotifyStatus(NotifyStatusEnum.PENDING.name());
        order.setRetryCount(0);
        order.setMaxRetryCount(3);
        order.setRemark(dto.getRemark());
        rechargeOrderMapper.insert(order);

        // 记录创建日志
        saveLog(order, OrderLogTypeEnum.OTHER, "INFO",
                "创建充值订单",
                "用户: " + dto.getUserName() + ", 金额: " + dto.getAmount()
                        + ", 年限: " + dto.getYears() + "年, 设备: " + dto.getDeviceNo(),
                dto.getUserName());

        log.info("创建充值订单成功, orderNo={}, userId={}, amount={}", orderNo, dto.getUserId(), dto.getAmount());

        MobileOrderDetailVO vo = new MobileOrderDetailVO();
        BeanUtil.copyProperties(order, vo);
        return vo;
    }

    @Override
    public Map<String, String> unifiedOrder(Long orderId, String openid) {
        RechargeOrder order = rechargeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!PayStatusEnum.PENDING.name().equals(order.getPayStatus())) {
            throw new RuntimeException("订单状态不可支付");
        }

        if (wechatPayConfig.isMock()) {
            // ===== 模拟模式：返回模拟预支付参数 =====
            Map<String, String> mockParams = new LinkedHashMap<>();
            mockParams.put("appId", wechatPayConfig.getAppId());
            mockParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            mockParams.put("nonceStr", IdUtil.fastSimpleUUID().substring(0, 16));
            mockParams.put("package", "prepay_id=mock_" + order.getOrderNo());
            mockParams.put("signType", "RSA");
            mockParams.put("paySign", "MOCK_SIGN_" + IdUtil.fastSimpleUUID());

            log.info("[模拟模式] 微信统一下单成功, orderNo={}, mockPrepayId=mock_{}",
                    order.getOrderNo(), order.getOrderNo());

            saveLog(order, OrderLogTypeEnum.PAY_CALLBACK, "INFO",
                    "模拟微信统一下单", "生成模拟prepay_id: mock_" + order.getOrderNo(), "SYSTEM");

            return mockParams;
        }

        // ===== 真实模式：调用微信支付API =====
        if (wechatPaySdkConfig == null) {
            throw new RuntimeException("微信支付SDK未配置");
        }

        try {
            // 懒加载 JsapiService
            if (jsapiService == null) {
                jsapiService = new JsapiService.Builder().config(wechatPaySdkConfig).build();
            }

            PrepayRequest prepayRequest = new PrepayRequest();
            prepayRequest.setAppid(wechatPayConfig.getAppId());
            prepayRequest.setMchid(wechatPayConfig.getMchId());
            prepayRequest.setDescription("定位器年费充值-" + order.getDeviceNo());
            prepayRequest.setOutTradeNo(order.getOrderNo());
            prepayRequest.setNotifyUrl(wechatPayConfig.getNotifyUrl());

            Amount amount = new Amount();
            amount.setTotal(order.getAmount().multiply(new BigDecimal("100")).intValue()); // 分
            amount.setCurrency("CNY");
            prepayRequest.setAmount(amount);

            Payer payer = new Payer();
            payer.setOpenid(openid);
            prepayRequest.setPayer(payer);

            PrepayResponse response = jsapiService.prepay(prepayRequest);
            log.info("微信统一下单成功, orderNo={}, prepayId={}", order.getOrderNo(), response.getPrepayId());

            saveLog(order, OrderLogTypeEnum.PAY_CALLBACK, "INFO",
                    "微信统一下单", "prepay_id: " + response.getPrepayId(), "SYSTEM");

            // 组装小程序调起支付参数
            Map<String, String> params = new LinkedHashMap<>();
            params.put("appId", wechatPayConfig.getAppId());
            params.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("nonceStr", IdUtil.fastSimpleUUID().substring(0, 16));
            params.put("package", "prepay_id=" + response.getPrepayId());
            params.put("signType", "RSA");
            // 签名由SDK内部处理
            params.put("paySign", "");
            return params;

        } catch (Exception e) {
            log.error("微信统一下单失败, orderNo={}", order.getOrderNo(), e);
            saveLog(order, OrderLogTypeEnum.PAY_CALLBACK, "ERROR",
                    "微信统一下单失败", e.getMessage(), "SYSTEM");
            throw new RuntimeException("微信统一下单失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handlePayCallback(String body, String signature, String serial,
                                    String timestamp, String nonce) {
        if (wechatPayConfig.isMock()) {
            // ===== 模拟模式：解析模拟回调 =====
            return handleMockCallback(body);
        }

        // ===== 真实模式：通过主动查询订单状态来验证支付 =====
        try {
            if (wechatPaySdkConfig == null) {
                throw new RuntimeException("微信支付SDK未配置");
            }
            if (jsapiServiceExtension == null) {
                jsapiServiceExtension = new JsapiServiceExtension.Builder()
                        .config(wechatPaySdkConfig)
                        .build();
            }

            // 解析回调报文获取订单号
            JSONObject callbackJson = JSONUtil.parseObj(body);
            String orderNo = callbackJson.getStr("out_trade_no");

            if (orderNo == null || orderNo.isEmpty()) {
                return "{\"code\":\"FAIL\",\"message\":\"回调报文缺少out_trade_no\"}";
            }

            // 主动查询微信支付订单状态确认支付成功
            QueryOrderByOutTradeNoRequest queryRequest = new QueryOrderByOutTradeNoRequest();
            queryRequest.setMchid(wechatPayConfig.getMchId());
            queryRequest.setOutTradeNo(orderNo);

            Transaction transaction = jsapiServiceExtension.queryOrderByOutTradeNo(queryRequest);

            if ("SUCCESS".equals(transaction.getTradeState())) {
                return processPayCallback(orderNo, transaction.getTransactionId());
            } else {
                log.warn("支付回调查询订单状态非成功, orderNo={}, tradeState={}",
                        orderNo, transaction.getTradeState());
                return "{\"code\":\"FAIL\",\"message\":\"订单状态非成功: " + transaction.getTradeState() + "\"}";
            }

        } catch (Exception e) {
            log.error("支付回调处理异常", e);
            return "{\"code\":\"FAIL\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 处理模拟支付回调
     */
    private String handleMockCallback(String body) {
        try {
            JSONObject json = JSONUtil.parseObj(body);
            String orderNo = json.getStr("out_trade_no");
            String transactionId = json.getStr("transaction_id", "MOCK_TXN_" + IdUtil.fastSimpleUUID().substring(0, 10));
            return processPayCallback(orderNo, transactionId);
        } catch (Exception e) {
            log.error("解析模拟回调失败", e);
            return "{\"code\":\"FAIL\",\"message\":\"回调解析失败\"}";
        }
    }

    /**
     * 统一支付回调处理：更新订单状态 → 调用充值 → 更新预存款和设备有效期
     */
    private String processPayCallback(String orderNo, String transactionId) {
        // 1. 查找订单
        RechargeOrder order = rechargeOrderMapper.selectOne(
                new LambdaQueryWrapper<RechargeOrder>().eq(RechargeOrder::getOrderNo, orderNo));
        if (order == null) {
            log.warn("支付回调: 订单不存在, orderNo={}", orderNo);
            return "{\"code\":\"FAIL\",\"message\":\"订单不存在\"}";
        }

        // 2. 幂等处理：已支付则直接返回成功
        if (PayStatusEnum.PAID.name().equals(order.getPayStatus())) {
            log.info("支付回调: 重复通知, orderNo={}", orderNo);
            return "{\"code\":\"SUCCESS\",\"message\":\"OK\"}";
        }

        // 3. 更新支付状态
        order.setPayStatus(PayStatusEnum.PAID.name());
        order.setWxTransactionId(transactionId);
        order.setPayTime(LocalDateTime.now());
        rechargeOrderMapper.updateById(order);

        saveLog(order, OrderLogTypeEnum.PAY_CALLBACK, "INFO",
                "支付成功",
                "微信交易号: " + transactionId + ", 金额: " + order.getAmount(), "SYSTEM");

        log.info("支付回调处理成功, orderNo={}, transactionId={}", orderNo, transactionId);

        // 4. 调用运营商充值
        try {
            executeRecharge(order);
        } catch (Exception e) {
            log.error("充值执行异常, orderNo={}", orderNo, e);
        }

        return "{\"code\":\"SUCCESS\",\"message\":\"OK\"}";
    }

    /**
     * 执行充值（支付成功后调用）
     */
    private void executeRecharge(RechargeOrder order) {
        order.setRechargeStatus(RechargeStatusEnum.RETRYING.name());
        rechargeOrderMapper.updateById(order);

        saveLog(order, OrderLogTypeEnum.RECHARGE_CALL, "INFO",
                "开始调用运营商充值", "订单号: " + order.getOrderNo(), "SYSTEM");

        try {
            boolean success = simulateOperatorRecharge(order);
            if (success) {
                order.setRechargeStatus(RechargeStatusEnum.SUCCESS.name());
                order.setRechargeTime(LocalDateTime.now());
                rechargeOrderMapper.updateById(order);

                // 扣减预存款
                updatePreDepositAccount(order);

                // 更新设备有效期
                updateDeviceValidityAfterRecharge(order);

                // 通知客户系统
                notifyCustomerSystem(order);

                saveLog(order, OrderLogTypeEnum.RECHARGE_RESULT, "INFO",
                        "充值成功", "运营商返回成功, 设备: " + order.getDeviceNo(), "SYSTEM");
            } else {
                order.setRechargeStatus(RechargeStatusEnum.FAILED.name());
                order.setErrorMsg("运营商API返回失败");
                rechargeOrderMapper.updateById(order);

                saveLog(order, OrderLogTypeEnum.RECHARGE_RESULT, "ERROR",
                        "充值失败", "运营商API返回失败", "SYSTEM");
            }
        } catch (Exception e) {
            log.error("运营商充值异常, orderNo={}", order.getOrderNo(), e);
            order.setRechargeStatus(RechargeStatusEnum.FAILED.name());
            order.setErrorMsg("充值异常: " + e.getMessage());
            rechargeOrderMapper.updateById(order);

            saveLog(order, OrderLogTypeEnum.RECHARGE_RESULT, "ERROR",
                    "充值异常", e.getMessage(), "SYSTEM");
        }
    }

    @Override
    public Page<MobileOrderDetailVO> queryUserOrders(MobileOrderQueryDTO dto) {
        Page<RechargeOrder> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<RechargeOrder> wrapper = new LambdaQueryWrapper<>();

        if (dto.getUserId() != null) {
            wrapper.eq(RechargeOrder::getUserId, dto.getUserId());
        }
        if (dto.getDeviceId() != null) {
            wrapper.eq(RechargeOrder::getDeviceId, dto.getDeviceId());
        }
        if (dto.getPayStatus() != null && !dto.getPayStatus().isEmpty()) {
            wrapper.eq(RechargeOrder::getPayStatus, dto.getPayStatus());
        }
        wrapper.orderByDesc(RechargeOrder::getCreateTime);

        Page<RechargeOrder> result = rechargeOrderMapper.selectPage(page, wrapper);

        Page<MobileOrderDetailVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<MobileOrderDetailVO> voList = new ArrayList<>();
        for (RechargeOrder order : result.getRecords()) {
            MobileOrderDetailVO vo = new MobileOrderDetailVO();
            BeanUtil.copyProperties(order, vo);
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public DeviceSimplifyVO getDeviceValidity(Long deviceId) {
        DeviceValidity validity = deviceValidityMapper.selectByDeviceId(deviceId);
        if (validity == null) {
            return null;
        }
        DeviceSimplifyVO vo = new DeviceSimplifyVO();
        BeanUtil.copyProperties(validity, vo);
        if (validity.getValidTo() != null) {
            vo.setRemainingDays(ChronoUnit.DAYS.between(LocalDate.now(), validity.getValidTo()));
        }
        return vo;
    }

    // ========== 私有辅助方法 ==========

    private void updatePreDepositAccount(RechargeOrder order) {
        PreDepositAccount account = preDepositAccountMapper.selectById(1);
        if (account == null) return;

        BigDecimal beforeBalance = account.getBalance();
        account.setBalance(account.getBalance().subtract(order.getAmount()));
        account.setTotalConsume(account.getTotalConsume().add(order.getAmount()));
        if (account.getBalance().compareTo(new BigDecimal("500")) < 0) {
            account.setStatus("LOW_BALANCE");
        }
        preDepositAccountMapper.updateById(account);

        PreDepositRecord record = new PreDepositRecord();
        record.setAccountId(account.getId());
        record.setOrderId(order.getId());
        record.setOrderNo(order.getOrderNo());
        record.setType("CONSUME");
        record.setAmount(order.getAmount());
        record.setBalanceBefore(beforeBalance);
        record.setBalanceAfter(account.getBalance());
        record.setRemark("小程序充值消耗: 设备" + order.getDeviceNo());
        preDepositRecordMapper.insert(record);
    }

    private void updateDeviceValidityAfterRecharge(RechargeOrder order) {
        DeviceValidity validity = deviceValidityMapper.selectByDeviceId(order.getDeviceId());
        LocalDate now = LocalDate.now();

        if (validity == null) {
            validity = new DeviceValidity();
            validity.setDeviceId(order.getDeviceId());
            validity.setDeviceNo(order.getDeviceNo());
            validity.setSimCardNo(order.getSimCardNo());
            validity.setValidFrom(now);
            validity.setValidTo(now.plusYears(order.getYears()));
            validity.setLastRechargeTime(LocalDateTime.now());
            validity.setTotalRechargeAmount(order.getAmount());
            validity.setStatus("NORMAL");
            deviceValidityMapper.insert(validity);
        } else {
            LocalDate newValidTo;
            if (validity.getValidTo() != null && validity.getValidTo().isAfter(now)) {
                newValidTo = validity.getValidTo().plusYears(order.getYears());
            } else {
                newValidTo = now.plusYears(order.getYears());
            }
            validity.setValidTo(newValidTo);
            validity.setValidFrom(validity.getValidFrom() != null ? validity.getValidFrom() : now);
            validity.setLastRechargeTime(LocalDateTime.now());
            validity.setTotalRechargeAmount(
                    validity.getTotalRechargeAmount() == null
                            ? order.getAmount()
                            : validity.getTotalRechargeAmount().add(order.getAmount()));
            validity.setStatus("NORMAL");
            deviceValidityMapper.updateById(validity);
        }
    }

    private void notifyCustomerSystem(RechargeOrder order) {
        try {
            log.info("通知客户系统更新设备有效期, orderNo={}, deviceId={}", order.getOrderNo(), order.getDeviceId());
            order.setNotifyStatus("SUCCESS");
            order.setNotifyTime(LocalDateTime.now());
            rechargeOrderMapper.updateById(order);

            saveLog(order, OrderLogTypeEnum.NOTIFY_CUSTOMER, "INFO",
                    "通知客户系统成功", "设备: " + order.getDeviceId(), "SYSTEM");
        } catch (Exception e) {
            log.error("通知客户系统失败", e);
            order.setNotifyStatus("FAILED");
            rechargeOrderMapper.updateById(order);
            saveLog(order, OrderLogTypeEnum.NOTIFY_CUSTOMER, "ERROR",
                    "通知客户系统失败", e.getMessage(), "SYSTEM");
        }
    }

    private boolean simulateOperatorRecharge(RechargeOrder order) {
        log.info("[模拟] 调用运营商API充值, orderNo={}, deviceId={}, amount={}",
                order.getOrderNo(), order.getDeviceId(), order.getAmount());
        return true;
    }

    private void saveLog(RechargeOrder order, OrderLogTypeEnum logType, String level,
                         String title, String content, String operator) {
        OrderLog logEntry = new OrderLog();
        if (order != null) {
            logEntry.setOrderId(order.getId());
            logEntry.setOrderNo(order.getOrderNo());
        }
        logEntry.setLogType(logType.name());
        logEntry.setLogLevel(level);
        logEntry.setTitle(title);
        logEntry.setContent(content);
        logEntry.setOperator(operator);
        orderLogMapper.insert(logEntry);
    }
}
