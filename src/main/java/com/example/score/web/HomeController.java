package com.example.score.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.score.common.LoginUser;
import com.example.score.common.SessionUtil;

import jakarta.servlet.http.HttpSession;

/**
 * 首页：按登录角色跳转到各自的工作台。
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index(HttpSession session) {
        LoginUser user = SessionUtil.get(session);
        if (user == null) {
            return "redirect:/login";
        }
        return "redirect:" + user.getRole().getHomePath();
    }
}
