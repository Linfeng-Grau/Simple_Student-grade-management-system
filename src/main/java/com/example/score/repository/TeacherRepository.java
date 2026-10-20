package com.example.score.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.score.entity.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, String> {
}
