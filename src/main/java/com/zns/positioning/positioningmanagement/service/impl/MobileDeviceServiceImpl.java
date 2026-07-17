package com.zns.positioning.positioningmanagement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zns.positioning.positioningmanagement.entity.GpsDevice;
import com.zns.positioning.positioningmanagement.entity.GpsDeviceAdditional;
import com.zns.positioning.positioningmanagement.entity.SysUser;
import com.zns.positioning.positioningmanagement.entity.UserBindDevice;
import com.zns.positioning.positioningmanagement.mapper.GpsDeviceAdditionalMapper;
import com.zns.positioning.positioningmanagement.mapper.GpsDeviceMapper;
import com.zns.positioning.positioningmanagement.mapper.SysUserMapper;
import com.zns.positioning.positioningmanagement.mapper.UserBindDeviceMapper;
import com.zns.positioning.positioningmanagement.service.MobileDeviceService;
import com.zns.positioning.positioningmanagement.vo.DeviceDetailVO;
import com.zns.positioning.positioningmanagement.vo.DeviceSimplifyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 移动端-设备 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MobileDeviceServiceImpl implements MobileDeviceService {

    private final GpsDeviceMapper gpsDeviceMapper;
    private final GpsDeviceAdditionalMapper gpsDeviceAdditionalMapper;
    private final UserBindDeviceMapper userBindDeviceMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<DeviceSimplifyVO> listUserDevices(Integer userId) {
        // 1. 查询用户绑定的设备关系
        List<UserBindDevice> bindings = userBindDeviceMapper.selectList(
                new LambdaQueryWrapper<UserBindDevice>()
                        .eq(UserBindDevice::getUserId, userId)
        );

        if (bindings.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 提取 deviceId → 绑定关系映射
        Map<Integer, UserBindDevice> bindMap = bindings.stream()
                .collect(Collectors.toMap(UserBindDevice::getDeviceId, b -> b, (a, b) -> a));

        // 3. 批量查询设备
        List<Integer> deviceIds = new ArrayList<>(bindMap.keySet());
        List<GpsDevice> devices = gpsDeviceMapper.selectBatchIds(deviceIds);

        // 4. 组装 VO
        return devices.stream()
                .map(device -> toSimplifyVO(device, bindMap.get(device.getDeviceId())))
                .collect(Collectors.toList());
    }

    @Override
    public DeviceDetailVO getDeviceDetail(Integer deviceId) {
        // 1. 查询设备基础信息
        GpsDevice device = gpsDeviceMapper.selectById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        // 2. 组装基本字段
        DeviceDetailVO vo = new DeviceDetailVO();
        BeanUtil.copyProperties(device, vo);

        // 3. 查询设备主人名称
        if (device.getDeviceOwner() != null) {
            SysUser owner = sysUserMapper.selectById(device.getDeviceOwner().intValue());
            if (owner != null) {
                vo.setOwnerName(owner.getUserName());
            }
        }

        // 4. 查询附加属性
        List<GpsDeviceAdditional> additionals = gpsDeviceAdditionalMapper.selectList(
                new LambdaQueryWrapper<GpsDeviceAdditional>()
                        .eq(GpsDeviceAdditional::getDeviceId, deviceId)
        );
        if (!additionals.isEmpty()) {
            List<Map<String, String>> attrList = additionals.stream().map(a -> {
                Map<String, String> m = new HashMap<>(2);
                m.put("key", a.getDocKey());
                m.put("value", a.getDocValue());
                return m;
            }).collect(Collectors.toList());
            vo.setAdditional(attrList);
        }

        // 5. 查询绑定用户
        List<UserBindDevice> bindings = userBindDeviceMapper.selectList(
                new LambdaQueryWrapper<UserBindDevice>()
                        .eq(UserBindDevice::getDeviceId, deviceId)
        );
        if (!bindings.isEmpty()) {
            // 批量查询用户
            List<Integer> userIds = bindings.stream()
                    .map(UserBindDevice::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            Map<Integer, SysUser> userMap = users.stream()
                    .collect(Collectors.toMap(SysUser::getUserId, u -> u));

            List<DeviceDetailVO.BindUserVO> bindUserList = bindings.stream().map(bind -> {
                DeviceDetailVO.BindUserVO bu = new DeviceDetailVO.BindUserVO();
                bu.setUserId(bind.getUserId());
                bu.setMainControl(bind.getMainControl());
                bu.setRelationShip(bind.getRelationShip());
                SysUser user = userMap.get(bind.getUserId());
                if (user != null) {
                    bu.setUserName(user.getUserName());
                    bu.setMobile(user.getMobile());
                }
                return bu;
            }).collect(Collectors.toList());
            vo.setBindUsers(bindUserList);
        }

        return vo;
    }

    // ==================== 私有方法 ====================

    /**
     * 设备实体 → 列表 VO
     */
    private DeviceSimplifyVO toSimplifyVO(GpsDevice device, UserBindDevice binding) {
        DeviceSimplifyVO vo = new DeviceSimplifyVO();
        vo.setDeviceId(device.getDeviceId());
        vo.setDeviceImei(device.getDeviceImei());
        vo.setDeviceName(device.getDeviceName());
        vo.setDeviceType(device.getDeviceType());
        vo.setDeviceStatus(device.getDeviceStatus());
        vo.setPlate(device.getPlate());
        vo.setDeviceColour(device.getDeviceColour());
        vo.setHeadPic(device.getHeadPic());
        vo.setLastLongitude(device.getLastLongitude());
        vo.setLastLatitude(device.getLastLatitude());
        vo.setLastPositionDesc(device.getLastPositionDesc());
        vo.setLastLocationTime(device.getLastLocationTime());
        vo.setLastDeviceVol(device.getLastDeviceVol());
        vo.setLastDeviceSms(device.getLastDeviceSms());
        vo.setLastSpeed(device.getLastSpeed());
        vo.setLastMotionStatus(device.getLastMotionStatus());
        vo.setExpireDate(device.getExpireDate());

        // 绑定关系
        if (binding != null) {
            vo.setMainControl(binding.getMainControl());
            vo.setRelationShip(binding.getRelationShip());
        }
        return vo;
    }
}
