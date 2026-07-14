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
import com.zns.positioning.positioningmanagement.common.enums.OperationLogTypeEnum;
import com.zns.positioning.positioningmanagement.common.enums.NotifyStatusEnum;
import com.zns.positioning.positioningmanagement.common.enums.PayStatusEnum;
import com.zns.positioning.positioningmanagement.common.enums.RechargeRecordStatusEnum;
import com.zns.positioning.positioningmanagement.common.enums.RechargeStatusEnum;
import com.zns.positioning.positioningmanagement.common.util.OperatorApiClient;
import com.zns.positioning.positioningmanagement.config.WechatPayConfig;
import com.zns.positioning.positioningmanagement.dto.CreateOrderDTO;
import com.zns.positioning.positioningmanagement.dto.MobileOrderQueryDTO;
import com.zns.positioning.positioningmanagement.dto.OperatorApiResponse;
import com.zns.positioning.positioningmanagement.entity.*;
import com.zns.positioning.positioningmanagement.entity.operator.CardPackage;
import com.zns.positioning.positioningmanagement.entity.operator.CardPackageResp;
import com.zns.positioning.positioningmanagement.mapper.*;
import com.zns.positioning.positioningmanagement.service.MobileRechargeService;
import com.zns.positioning.positioningmanagement.service.OperationLogService;
import com.zns.positioning.positioningmanagement.service.RechargeRecordService;
import com.zns.positioning.positioningmanagement.vo.MobileOrderDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobileRechargeServiceImpl implements MobileRechargeService {

    private final RechargeOrderMapper rechargeOrderMapper;
    private final PackagePlanMapper packagePlanMapper;
    private final OperationLogService operationLogService;
    private final WechatPayConfig wechatPayConfig;
    private final OperatorApiClient operatorApiClient;
    private final RechargeRecordService rechargeRecordService;

    @Autowired(required = false)
    private Config wechatPaySdkConfig;
    private JsapiService jsapiService;
    private JsapiServiceExtension jsapiServiceExtension;

    /**
     * 物联卡状态码 -> 中文说明映射
     */
    private static final Map<String, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put("-1", "错误数据");
        STATUS_MAP.put("1", "可激活");
        STATUS_MAP.put("2", "已激活");
        STATUS_MAP.put("3", "已停用");
        STATUS_MAP.put("4", "违章停机");
        STATUS_MAP.put("5", "已失效");
        STATUS_MAP.put("6", "可测试");
        STATUS_MAP.put("7", "运营商注销");
        STATUS_MAP.put("8", "用户注销");
        STATUS_MAP.put("9", "欠停");
        STATUS_MAP.put("10", "挂失");
        STATUS_MAP.put("11", "库存");
        STATUS_MAP.put("12", "故障卡");
        STATUS_MAP.put("13", "已断网");
        STATUS_MAP.put("14", "未实名停机");
        STATUS_MAP.put("15", "预注销");
        STATUS_MAP.put("16", "超量停机");
        STATUS_MAP.put("17", "停机保号");
        STATUS_MAP.put("18", "已锁定");
        STATUS_MAP.put("19", "部分停用-聚合卡");
        STATUS_MAP.put("20", "未实名关闭网络");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MobileOrderDetailVO createOrder(CreateOrderDTO dto) {
        // 1. 查询当前号码是否可用
        OperatorApiResponse<JSONObject> cardStatusResp = operatorApiClient.queryCardStatus(dto.getIccid());
        if (cardStatusResp.isFail()) {
            String errMsg = cardStatusResp.getMsg();
            log.warn("号码不可用, iccid={}, code={}, msg={}", dto.getIccid(), cardStatusResp.getCode(), errMsg);
            throw new RuntimeException("号码状态异常: " + errMsg);
        }
        JSONObject data = cardStatusResp.getData();
        if (data == null) {
            log.warn("号码状态查询返回数据为空, iccid={}", dto.getIccid());
            throw new RuntimeException("号码状态查询失败，未获取到卡信息");
        }
        String status = data.getStr("code");
        // 只有1，2，3，6，11，13，14，16，17，20可用
        Set<String> availableStatuses = new HashSet<>(Arrays.asList(
                "1", "2", "3", "6", "11", "13", "14", "16", "17", "20"));
        if (!availableStatuses.contains(status)) {
            String statusDesc = STATUS_MAP.getOrDefault(status, "未知状态（" + status + "）");
            log.warn("号码不可用, iccid={}, status={}, desc={}", dto.getIccid(), status, statusDesc);
            throw new RuntimeException("当前号码状态不可用（" + statusDesc + "），无法创建订单");
        }

        // 2. 根据套餐ID查询套餐信息
        PackagePlan plan = packagePlanMapper.selectById(dto.getPlanId());
        if (plan == null) {
            throw new RuntimeException("套餐不存在");
        }
        if (plan.getStatus() == null || plan.getStatus() != 1) {
            throw new RuntimeException("该套餐已停用，请选择其他套餐");
        }

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
        order.setIccid(dto.getIccid());
        order.setPlanId(plan.getId());
        order.setPlanName(plan.getPlanName());
        order.setPlanAmount(plan.getPrice());
        order.setPlanYears(plan.getYears());
        order.setPayStatus(PayStatusEnum.PENDING.name());
        order.setRechargeStatus(RechargeStatusEnum.PENDING.name());
        order.setNotifyStatus(NotifyStatusEnum.PENDING.name());
        order.setRetryCount(0);
        order.setMaxRetryCount(3);
        order.setRemark(dto.getRemark());
        rechargeOrderMapper.insert(order);

        // 记录创建日志
        operationLogService.saveLog(OperationLogTypeEnum.ORDER_CREATE,
                order.getId(), order.getOrderNo(), order.getDeviceNo(),
                "INFO", "创建充值订单",
                "用户: " + dto.getUserName() + ", 套餐: " + plan.getPlanName()
                        + ", 金额: " + plan.getPrice() + ", 年限: " + plan.getYears()
                        + "年, 设备: " + dto.getDeviceNo(),
                dto.getUserName());

        log.info("创建充值订单成功, orderNo={}, userId={}, planId={}, planName={}, amount={}",
                orderNo, dto.getUserId(), plan.getId(), plan.getPlanName(), plan.getPrice());

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

            operationLogService.saveLog(OperationLogTypeEnum.ORDER_PAY_CALLBACK,
                    order.getId(), order.getOrderNo(), order.getDeviceNo(),
                    "INFO", "模拟微信统一下单",
                    "生成模拟prepay_id: mock_" + order.getOrderNo(), "SYSTEM");

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
            amount.setTotal(order.getPlanAmount().multiply(new BigDecimal("100")).intValue()); // 分
            amount.setCurrency("CNY");
            prepayRequest.setAmount(amount);

            Payer payer = new Payer();
            payer.setOpenid(openid);
            prepayRequest.setPayer(payer);

            PrepayResponse response = jsapiService.prepay(prepayRequest);
            log.info("微信统一下单成功, orderNo={}, prepayId={}", order.getOrderNo(), response.getPrepayId());

            operationLogService.saveLog(OperationLogTypeEnum.ORDER_PAY_CALLBACK,
                    order.getId(), order.getOrderNo(), order.getDeviceNo(),
                    "INFO", "微信统一下单", "prepay_id: " + response.getPrepayId(), "SYSTEM");

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
            operationLogService.saveLog(OperationLogTypeEnum.ORDER_PAY_CALLBACK,
                    order.getId(), order.getOrderNo(), order.getDeviceNo(),
                    "ERROR", "微信统一下单失败", e.getMessage(), "SYSTEM");
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


        operationLogService.saveLog(OperationLogTypeEnum.ORDER_PAY_CALLBACK,
                order.getId(), order.getOrderNo(), order.getDeviceNo(),
                "INFO", "支付成功",
                "微信交易号: " + transactionId + ", 金额: " + order.getPlanAmount(), "SYSTEM");

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
        order.setRetryCount(order.getRetryCount() != null ? order.getRetryCount() + 1 : 1);
        rechargeOrderMapper.updateById(order);

        // 查询当前设备的运营商套餐信息
        OperatorApiResponse<JSONObject> queryCardPackageResp = operatorApiClient.queryCardPackage(order.getIccid());
        if (!queryCardPackageResp.isFail()) {
            log.error("查询运营商套餐信息失败, orderNo={}, msg={}", order.getOrderNo(), queryCardPackageResp.getMsg());
            return;
        }

        if (queryCardPackageResp.isFail()) {
            String errMsg = queryCardPackageResp.getMsg();
            log.error("查询运营商套餐信息失败, orderNo={}, msg={}", order.getOrderNo(), queryCardPackageResp.getMsg());
            throw new RuntimeException("号码状态异常: " + errMsg);
        }

        CardPackageResp resp = queryCardPackageResp.getData().toBean(CardPackageResp.class);
        List<CardPackage> packageList = resp.getBasicsePackage();
        if(packageList.isEmpty()){
            log.error("请管理后台，没有配置基础套餐");
            throw new RuntimeException("请管理后台，没有配置基础套餐");
        }

        CardPackage cardpackage = getCardPackage(packageList, order.getPlanYears());
        if(cardpackage == null){
            log.error("请管理后台，没有配置套餐");
            throw new RuntimeException("请管理后台，没有配置套餐");
        }

        operationLogService.saveLog(OperationLogTypeEnum.ORDER_RECHARGE_CALL,
                order.getId(), order.getOrderNo(), order.getDeviceNo(),
                "INFO", "开始调用运营商充值", "订单号: " + order.getOrderNo(), "SYSTEM");

        // 调用运营商充值API
        try {
            // 使用卡商API进行真实充值
            OperatorApiResponse<JSONObject> rechargeResp = operatorApiClient.recharge(order.getIccid(),
                    cardpackage.getPackageId(), cardpackage.getPrice());

            String responseBody = JSONUtil.toJsonStr(rechargeResp);

            if (rechargeResp.isSuccess()) {
                order.setRechargeStatus(RechargeStatusEnum.SUCCESS.name());
                order.setRechargeTime(LocalDateTime.now());
                rechargeOrderMapper.updateById(order);

                // 记录充值成功
                rechargeRecordService.saveRecord(buildRechargeRecord(order,
                        RechargeRecordStatusEnum.SUCCESS.name(),
                        rechargeResp.getCode(), rechargeResp.getMsg(),
                        responseBody, null));

                // 通知客户系统
                notifyCustomerSystem(order);

                operationLogService.saveLog(OperationLogTypeEnum.ORDER_RECHARGE_RESULT,
                        order.getId(), order.getOrderNo(), order.getDeviceNo(),
                        "INFO", "充值成功", "运营商返回成功, 设备: " + order.getDeviceNo(), "SYSTEM");
            } else {
                String errMsg = rechargeResp.getMsg() != null ? rechargeResp.getMsg() : "运营商API返回失败";
                order.setRechargeStatus(RechargeStatusEnum.FAILED.name());
                order.setErrorMsg(errMsg);
                rechargeOrderMapper.updateById(order);

                // 记录充值失败
                rechargeRecordService.saveRecord(buildRechargeRecord(order,
                        RechargeRecordStatusEnum.FAILED.name(),
                        rechargeResp.getCode(), rechargeResp.getMsg(),
                        responseBody, errMsg));

                operationLogService.saveLog(OperationLogTypeEnum.ORDER_RECHARGE_RESULT,
                        order.getId(), order.getOrderNo(), order.getDeviceNo(),
                        "ERROR", "充值失败", "code=" + rechargeResp.getCode() + ", msg=" + errMsg, "SYSTEM");
            }
        } catch (Exception e) {
            log.error("运营商充值异常, orderNo={}", order.getOrderNo(), e);
            String errMsg = "充值异常: " + e.getMessage();
            order.setRechargeStatus(RechargeStatusEnum.FAILED.name());
            order.setErrorMsg(errMsg);
            rechargeOrderMapper.updateById(order);

            // 记录接口调用异常
            rechargeRecordService.saveRecord(buildRechargeRecord(order,
                    RechargeRecordStatusEnum.CALL_FAILED.name(),
                    null, null,
                    null, errMsg));

            operationLogService.saveLog(OperationLogTypeEnum.ORDER_RECHARGE_RESULT,
                    order.getId(), order.getOrderNo(), order.getDeviceNo(),
                    "ERROR", "充值异常", e.getMessage(), "SYSTEM");
        }
    }

    /**
     * 获取指定套餐
     */
    private CardPackage getCardPackage(List<CardPackage> packageList, Integer planYears) {
        for (CardPackage cardPackage : packageList) {
            // planYears 是年， cycle 是日，
            if(planYears == 1){
                if (cardPackage.getCycle()  == 360) {
                    return cardPackage;
                }
            }
        }
        return null;
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

    // ========== 私有辅助方法 ==========

    /**
     * 构建充值记录对象
     */
    private RechargeRecord buildRechargeRecord(RechargeOrder order, String rechargeStatus,
                                               String respCode, String respMsg,
                                               String responseBody, String errorMsg) {
        RechargeRecord record = new RechargeRecord();
        record.setOrderId(order.getId());
        record.setOrderNo(order.getOrderNo());
        record.setDeviceId(order.getDeviceId());
        record.setDeviceNo(order.getDeviceNo());
        record.setIccid(order.getIccid());
        record.setPackageId(order.getPlanId());
        record.setPlanName(order.getPlanName());
        record.setAmount(order.getPlanAmount());
        record.setRechargeStatus(rechargeStatus);
        record.setRespCode(respCode);
        record.setRespMsg(respMsg);
        record.setResponseBody(responseBody);
        record.setErrorMsg(errorMsg);
        record.setRechargeTime(LocalDateTime.now());
        return record;
    }

    private void notifyCustomerSystem(RechargeOrder order) {
        try {
            log.info("通知客户系统更新设备有效期, orderNo={}, deviceId={}", order.getOrderNo(), order.getDeviceId());
            order.setNotifyStatus("SUCCESS");
            order.setNotifyTime(LocalDateTime.now());
            rechargeOrderMapper.updateById(order);

            operationLogService.saveLog(OperationLogTypeEnum.ORDER_NOTIFY_CUSTOMER,
                    order.getId(), order.getOrderNo(), order.getDeviceNo(),
                    "INFO", "通知客户系统成功", "设备: " + order.getDeviceId(), "SYSTEM");
        } catch (Exception e) {
            log.error("通知客户系统失败", e);
            order.setNotifyStatus("FAILED");
            rechargeOrderMapper.updateById(order);
            operationLogService.saveLog(OperationLogTypeEnum.ORDER_NOTIFY_CUSTOMER,
                    order.getId(), order.getOrderNo(), order.getDeviceNo(),
                    "ERROR", "通知客户系统失败", e.getMessage(), "SYSTEM");
        }
    }


}
