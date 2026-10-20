package com.example.score.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 教师。 */
@Entity
@Table(name = "teacher")
public class Teacher {

    @Id
    @Column(name = "tno", length = 20)
    private String tno;

    @Column(name = "tname", nullable = false, length = 30)
    private String tname;

    protected Teacher() {
    }

    public Teacher(String tno, String tname) {
        this.tno = tno;
        this.tname = tname;
    }

    public String getTno() {
        return tno;
    }

    public String getTname() {
        return tname;
    }

    public void setTname(String tname) {
        this.tname = tname;
    }
}
