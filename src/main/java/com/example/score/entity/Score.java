package com.example.score.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 成绩：某生某课的平时、考试与总评；总评由 GradeCalculator 按课程比例算好后落库。
 */
@Entity
@Table(name = "score",
        uniqueConstraints = @UniqueConstraint(name = "uk_score_sno_cno", columnNames = {"sno", "cno"}),
        indexes = {
                @Index(name = "idx_score_sno", columnList = "sno"),
                @Index(name = "idx_score_cno", columnList = "cno")
        })
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sno", nullable = false, length = 20)
    private String sno;

    @Column(name = "cno", nullable = false, length = 20)
    private String cno;

    @Column(name = "usual_score", precision = 4, scale = 1)
    private BigDecimal usualScore;

    @Column(name = "exam_score", precision = 4, scale = 1)
    private BigDecimal examScore;

    @Column(name = "total_score", precision = 4, scale = 1)
    private BigDecimal totalScore;

    protected Score() {
    }

    public Score(String sno, String cno) {
        this.sno = sno;
        this.cno = cno;
    }

    public Long getId() {
        return id;
    }

    public String getSno() {
        return sno;
    }

    public String getCno() {
        return cno;
    }

    public BigDecimal getUsualScore() {
        return usualScore;
    }

    public BigDecimal getExamScore() {
        return examScore;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    /**
     * 一次性写入三个分数，保证总评与平时/考试始终同步。
     */
    public void updateScores(BigDecimal usualScore, BigDecimal examScore, BigDecimal totalScore) {
        this.usualScore = usualScore;
        this.examScore = examScore;
        this.totalScore = totalScore;
    }
}
