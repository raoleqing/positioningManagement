package com.zns.positioning.positioningmanagement.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 后台管理页面控制器
 */
@Controller
public class PageController {

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/admin";
    }
}
