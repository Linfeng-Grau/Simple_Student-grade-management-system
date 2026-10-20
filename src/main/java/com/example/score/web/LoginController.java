package com.example.score.web;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.score.common.LoginUser;
import com.example.score.common.Role;
import com.example.score.common.SessionUtil;
import com.example.score.repository.AppUserRepository;
import com.example.score.service.AuthService;
import com.example.score.web.view.DemoAccount;

import jakarta.servlet.http.HttpSession;

/**
 * 登录、退出与无权访问提示；角色由账号决定，登录后自动进入对应工作台。
 */
@Controller
public class LoginController {

    private final AuthService authService;
    private final AppUserRepository appUserRepository;

    public LoginController(AuthService authService, AppUserRepository appUserRepository) {
        this.authService = authService;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String redirect,
                            @RequestParam(required = false) String logout,
                            HttpSession session,
                            Model model) {
        LoginUser current = SessionUtil.get(session);
        if (current != null) {
            return "redirect:" + (current.isMustChangePassword()
                    ? "/password/change"
                    : current.getRole().getHomePath());
        }
        model.addAttribute("redirect", redirect == null ? "" : redirect);
        model.addAttribute("loggedOut", logout != null);
        model.addAttribute("demoAccounts", demoAccounts());
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam(required = false) String redirect,
                          HttpSession session,
                          Model model) {
        Optional<LoginUser> loginUser = authService.login(username, password);
        if (loginUser.isEmpty()) {
            model.addAttribute("error", "账号或密码不正确，请重新输入。");
            model.addAttribute("username", username);
            model.addAttribute("redirect", redirect == null ? "" : redirect);
            return "login";
        }

        LoginUser user = loginUser.get();
        session.setAttribute(LoginUser.SESSION_KEY, user);
        session.setMaxInactiveInterval(30 * 60);

        // 教师新建的学生账号：先改密码，再进业务页
        if (user.isMustChangePassword()) {
            return "redirect:/password/change";
        }

        String target = safeRedirect(redirect, user);
        return "redirect:" + (target != null ? target : user.getRole().getHomePath());
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=1";
    }

    @GetMapping("/forbidden")
    public String forbidden(HttpSession session, Model model) {
        model.addAttribute("errorTitle", "无权访问");
        model.addAttribute("errorMessage", "当前账号（"
                + (SessionUtil.get(session) == null ? "未登录" : SessionUtil.get(session).getRole().getLabel())
                + "）不能打开这个页面。");
        model.addAttribute("me", SessionUtil.get(session));
        return "error/biz";
    }

    /** 登录页演示学生按钮，班长状态实时取库。 */
    private List<DemoAccount> demoAccounts() {
        return appUserRepository.findByDemoAccountTrueOrderByUsernameAsc().stream()
                .map(user -> new DemoAccount(user.getUsername(), user.getName(),
                        user.getRole() == Role.MONITOR))
                .toList();
    }

    /** 只允许回跳到本站本角色的地址，防站外跳转。 */
    private static String safeRedirect(String redirect, LoginUser user) {
        if (redirect == null || redirect.isBlank()) {
            return null;
        }
        String target = redirect.trim();
        if (!target.startsWith("/") || target.startsWith("//") || target.contains("\\")) {
            return null;
        }
        String prefix = user.getRole().getPathPrefix();
        return target.equals(prefix) || target.startsWith(prefix + "/") ? target : null;
    }
}
