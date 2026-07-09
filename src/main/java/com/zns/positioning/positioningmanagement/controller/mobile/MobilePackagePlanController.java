package com.zns.positioning.positioningmanagement.controller.mobile;

import com.zns.positioning.positioningmanagement.common.R;
import com.zns.positioning.positioningmanagement.service.PackagePlanService;
import com.zns.positioning.positioningmanagement.vo.PackagePlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 套餐管理 Controller
 */
@RestController
@RequestMapping("/mobile/package-plan")
@RequiredArgsConstructor
public class MobilePackagePlanController {

    private final PackagePlanService packagePlanService;


    /** 查询所有启用的套餐（供小程序端使用） */
    @GetMapping("/list")
    public R<List<PackagePlanVO>> list() {
        return R.ok(packagePlanService.listEnabled());
    }


}
