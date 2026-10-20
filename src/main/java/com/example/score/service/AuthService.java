package com.example.score.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.score.common.LoginUser;
import com.example.score.entity.AppUser;
import com.example.score.repository.AppUserRepository;

/** 登录校验。 */
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;

    public AuthService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    /** 校验账号密码，成功返回登录用户（角色由账号决定，页面无需选择）。 */
    public Optional<LoginUser> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        String name = username.trim();
        if (name.isEmpty() || password.isEmpty()) {
            return Optional.empty();
        }
        return appUserRepository.findByUsername(name)
                .filter(user -> user.getPassword().equals(password))
                .map(AuthService::toLoginUser);
    }

    private static LoginUser toLoginUser(AppUser user) {
        return new LoginUser(user.getUsername(), user.getName(), user.getRole(),
                user.getRefNo(), user.getClassNo(), user.isMustChangePassword());
    }
}
