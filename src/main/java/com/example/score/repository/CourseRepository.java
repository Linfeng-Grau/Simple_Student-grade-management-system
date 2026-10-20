package com.example.score.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.score.entity.Course;

public interface CourseRepository extends JpaRepository<Course, String> {

    /** 教师端「我的课程」 */
    List<Course> findByTeacherNoOrderByCnoAsc(String teacherNo);

    /** 学生端 / 排名：某班级的全部课程 */
    List<Course> findByClassNoOrderByCnoAsc(String classNo);
}
