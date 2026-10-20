package com.example.score.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.score.common.BizException;
import com.example.score.common.LoginUser;
import com.example.score.common.PasswordPolicy;
import com.example.score.common.SessionUtil;
import com.example.score.entity.AppUser;
import com.example.score.repository.AppUserRepository;

import jakarta.servlet.http.HttpSession;

/**
 * 首次登录强制改密：初始密码 114514，改后清除强制标记并刷新会话。
 */
@Controller
public class PasswordChangeController {

    private final AppUserRepository appUserRepository;

    public PasswordChangeController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/password/change")
    public String page(HttpSession session, Model model) {
        LoginUser me = SessionUtil.get(session);
        if (me == null) {
            return "redirect:/login";
        }
        model.addAttribute("me", me);
        model.addAttribute("mustChange", me.isMustChangePassword());
        return "password/change";
    }

    @PostMapping("/password/change")
    public String change(@RequestParam String newPassword,
                         @RequestParam String confirmPassword,
                         HttpSession session,
                         Model model) {
        LoginUser me = SessionUtil.get(session);
        if (me == null) {
            return "redirect:/login";
        }

        String error = PasswordPolicy.validate(newPassword);
        if (error == null && !newPassword.equals(confirmPassword)) {
            error = "两次输入的新密码不一致。";
        }
        if (error != null) {
            model.addAttribute("me", me);
            model.addAttribute("mustChange", me.isMustChangePassword());
            model.addAttribute("error", error);
            return "password/change";
        }

        AppUser user = appUserRepository.findByUsername(me.getUsername())
                .orElseThrow(() -> new BizException("账号不存在，请重新登录。"));
        user.setPassword(newPassword);
        user.setMustChangePassword(false);
        appUserRepository.save(user);

        LoginUser updated = new LoginUser(user.getUsername(), user.getName(), user.getRole(),
                user.getRefNo(), user.getClassNo(), false);
        session.setAttribute(LoginUser.SESSION_KEY, updated);

        return "redirect:" + updated.getRole().getHomePath();
    }
}
