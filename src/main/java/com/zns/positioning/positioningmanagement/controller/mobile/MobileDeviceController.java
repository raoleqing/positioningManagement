package com.zns.positioning.positioningmanagement.controller.mobile;

import com.zns.positioning.positioningmanagement.common.R;
import com.zns.positioning.positioningmanagement.service.MobileDeviceService;
import com.zns.positioning.positioningmanagement.vo.DeviceDetailVO;
import com.zns.positioning.positioningmanagement.vo.DeviceSimplifyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 移动端-设备控制器
 */
@Slf4j
@RestController
@RequestMapping("/mobile/device")
@RequiredArgsConstructor
public class MobileDeviceController {

    private final MobileDeviceService mobileDeviceService;

    /**
     * 用户设备列表
     * GET /api/mobile/device/list?userId=xxx
     *
     * @param userId 用户ID
     * @return 该用户绑定的设备列表（含位置、电量等摘要 + 主控标识）
     */
    @GetMapping("/list")
    public R<List<DeviceSimplifyVO>> list(@RequestParam Integer userId) {
        List<DeviceSimplifyVO> devices = mobileDeviceService.listUserDevices(userId);
        return R.ok(devices);
    }

    /**
     * 设备详情
     * GET /api/mobile/device/detail?deviceId=xxx
     *
     * @param deviceId 设备ID
     * @return 设备完整信息（含附加属性、绑定用户列表）
     */
    @GetMapping("/detail")
    public R<DeviceDetailVO> detail(@RequestParam Integer deviceId) {
        DeviceDetailVO detail = mobileDeviceService.getDeviceDetail(deviceId);
        return R.ok(detail);
    }
}
