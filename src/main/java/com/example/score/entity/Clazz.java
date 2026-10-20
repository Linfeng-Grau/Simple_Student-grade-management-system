package com.example.score.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 班级。 */
@Entity
@Table(name = "clazz")
public class Clazz {

    @Id
    @Column(name = "class_no", length = 20)
    private String classNo;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    protected Clazz() {
        // JPA 需要无参构造
    }

    public Clazz(String classNo, String className) {
        this.classNo = classNo;
        this.className = className;
    }

    public String getClassNo() {
        return classNo;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
