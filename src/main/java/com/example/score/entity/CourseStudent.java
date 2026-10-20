package com.example.score.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 选课关系：成绩录入、查成绩、移出学生都以「课程 + 学生」为准，移出一门课不影响其它课程。
 */
@Entity
@Table(name = "course_student",
        uniqueConstraints = @UniqueConstraint(name = "uk_cs_cno_sno", columnNames = {"cno", "sno"}),
        indexes = {
                @Index(name = "idx_cs_cno", columnList = "cno"),
                @Index(name = "idx_cs_sno", columnList = "sno")
        })
public class CourseStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cno", nullable = false, length = 20)
    private String cno;

    @Column(name = "sno", nullable = false, length = 20)
    private String sno;

    protected CourseStudent() {
    }

    public CourseStudent(String cno, String sno) {
        this.cno = cno;
        this.sno = sno;
    }

    public Long getId() {
        return id;
    }

    public String getCno() {
        return cno;
    }

    public String getSno() {
        return sno;
    }
}
