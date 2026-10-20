package com.example.score.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.score.common.Role;
import com.example.score.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByRefNo(String refNo);

    List<AppUser> findByRoleAndClassNo(Role role, String classNo);

    List<AppUser> findByRole(Role role);

    List<AppUser> findByDemoAccountTrueOrderByUsernameAsc();
}
