package com.example.score.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.score.entity.Clazz;

public interface ClazzRepository extends JpaRepository<Clazz, String> {
}
