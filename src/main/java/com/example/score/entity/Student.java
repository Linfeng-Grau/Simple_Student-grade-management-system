package com.example.score.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/** 学生；学号同时是登录账号。 */
@Entity
@Table(name = "student", indexes = @Index(name = "idx_student_class", columnList = "class_no"))
public class Student {

    @Id
    @Column(name = "sno", length = 20)
    private String sno;

    @Column(name = "sname", nullable = false, length = 30)
    private String sname;

    @Column(name = "class_no", nullable = false, length = 20)
    private String classNo;

    protected Student() {
    }

    public Student(String sno, String sname, String classNo) {
        this.sno = sno;
        this.sname = sname;
        this.classNo = classNo;
    }

    public String getSno() {
        return sno;
    }

    public String getSname() {
        return sname;
    }

    public String getClassNo() {
        return classNo;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public void setClassNo(String classNo) {
        this.classNo = classNo;
    }
}
