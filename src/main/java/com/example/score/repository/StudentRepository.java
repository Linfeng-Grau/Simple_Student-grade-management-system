package com.example.score.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.score.entity.Student;

public interface StudentRepository extends JpaRepository<Student, String> {

    List<Student> findByClassNoOrderBySnoAsc(String classNo);

    long countByClassNo(String classNo);
}
