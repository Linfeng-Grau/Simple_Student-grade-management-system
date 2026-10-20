package com.example.score.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * 课程：teacherNo 决定谁能录成绩，classNo 决定给哪个班上课，比例保存在课程上。
 */
@Entity
@Table(name = "course", indexes = {
        @Index(name = "idx_course_teacher", columnList = "teacher_no"),
        @Index(name = "idx_course_class", columnList = "class_no")
})
public class Course {

    @Id
    @Column(name = "cno", length = 20)
    private String cno;

    @Column(name = "cname", nullable = false, length = 50)
    private String cname;

    /** 学分，例如 3.5 */
    @Column(name = "credit", nullable = false, precision = 4, scale = 1)
    private BigDecimal credit;

    @Column(name = "class_no", nullable = false, length = 20)
    private String classNo;

    @Column(name = "teacher_no", nullable = false, length = 20)
    private String teacherNo;

    /** 平时成绩占比（百分数，0~100） */
    @Column(name = "usual_ratio", nullable = false)
    private int usualRatio;

    /** 考试成绩占比（百分数，0~100），通常 = 100 - usualRatio */
    @Column(name = "exam_ratio", nullable = false)
    private int examRatio;

    protected Course() {
    }

    public Course(String cno, String cname, BigDecimal credit, String classNo, String teacherNo,
                  int usualRatio, int examRatio) {
        this.cno = cno;
        this.cname = cname;
        this.credit = credit;
        this.classNo = classNo;
        this.teacherNo = teacherNo;
        this.usualRatio = usualRatio;
        this.examRatio = examRatio;
    }

    public String getCno() {
        return cno;
    }

    public String getCname() {
        return cname;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public String getClassNo() {
        return classNo;
    }

    public String getTeacherNo() {
        return teacherNo;
    }

    public int getUsualRatio() {
        return usualRatio;
    }

    public int getExamRatio() {
        return examRatio;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public void setClassNo(String classNo) {
        this.classNo = classNo;
    }

    public void setTeacherNo(String teacherNo) {
        this.teacherNo = teacherNo;
    }

    public void setUsualRatio(int usualRatio) {
        this.usualRatio = usualRatio;
    }

    public void setExamRatio(int examRatio) {
        this.examRatio = examRatio;
    }
}
