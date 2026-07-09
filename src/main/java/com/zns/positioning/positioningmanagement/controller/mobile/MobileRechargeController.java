package com.zns.positioning.positioningmanagement.controller.mobile;

import cn.hutool.json.JSONUtil;
import com.zns.positioning.positioningmanagement.common.R;
import com.zns.positioning.positioningmanagement.dto.CreateOrderDTO;
import com.zns.positioning.positioningmanagement.dto.MobileOrderQueryDTO;
import com.zns.positioning.positioningmanagement.service.MobileRechargeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小程序端-充值控制器
 * <p>
 * 接口路径统一以 /mobile 开头，与 admin 端 /order 等区分
 */
@Slf4j
@RestController
@RequestMapping("/mobile")
@RequiredArgsConstructor
public class MobileRechargeController {

    private final MobileRechargeService mobileRechargeService;

    // ===================== 1. 创建订单 =====================

    /**
     * 创建充值订单
     * POST /api/mobile/order/create
     */
    @PostMapping("/order/create")
    public R<?> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        return R.ok(mobileRechargeService.createOrder(dto));
    }

    // ===================== 2. 微信统一下单 =====================

    /**
     * 微信统一下单（获取小程序调起支付参数）
     * POST /api/mobile/order/unified
     *
     * @param params orderId 订单ID, openid 用户openid
     */
    @PostMapping("/order/unified")
    public R<?> unifiedOrder(@RequestBody Map<String, String> params) {
        Long orderId = Long.valueOf(params.get("orderId"));
        String openid = params.getOrDefault("openid", "");
        Map<String, String> payParams = mobileRechargeService.unifiedOrder(orderId, openid);
        return R.ok(payParams);
    }

    // ===================== 3. 微信支付回调 =====================

    /**
     * 微信支付异步通知回调
     * POST /api/mobile/pay/callback
     * <p>
     * 微信支付在收到支付成功后，通过此接口通知服务端更新订单状态。
     * 需要在微信商户后台配置回调URL（development模式可通过 POST 手动模拟）
     */
    @PostMapping("/pay/callback")
    public String payCallback(@RequestBody String body,
                              @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
                              @RequestHeader(value = "Wechatpay-Serial", required = false) String serial,
                              @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
                              @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce) {
        return mobileRechargeService.handlePayCallback(body, signature, serial, timestamp, nonce);
    }

    // ===================== 4. 充值记录明细 =====================

    /**
     * 查询用户充值记录（分页）
     * GET /api/mobile/recharge/records?userId=1&pageNum=1&pageSize=10
     */
    @GetMapping("/recharge/records")
    public R<?> queryRecords(@RequestParam Long userId,
                             @RequestParam(required = false) Long deviceId,
                             @RequestParam(required = false) String payStatus,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        MobileOrderQueryDTO dto = new MobileOrderQueryDTO();
        dto.setUserId(userId);
        dto.setDeviceId(deviceId);
        dto.setPayStatus(payStatus);
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        return R.ok(mobileRechargeService.queryUserOrders(dto));
    }

    // ===================== 模拟支付回调（仅开发环境） =====================

    /**
     * 模拟支付成功回调（开发/测试用）
     * POST /api/mobile/pay/mock-callback
     */
    @PostMapping("/pay/mock-callback")
    public String mockPayCallback(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        String body = JSONUtil.createObj()
                .set("out_trade_no", orderNo)
                .set("transaction_id", "MOCK_" + System.currentTimeMillis())
                .toString();
        return mobileRechargeService.handlePayCallback(body, null, null, null, null);
    }
}
