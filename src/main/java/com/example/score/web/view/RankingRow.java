package com.example.score.web.view;

import java.math.BigDecimal;
import java.util.List;

/**
 * 班级排名表的一行。
 *
 * @param rank     名次，0 表示尚无成绩不参与排名
 * @param self     是否为当前登录的班长本人
 */
public record RankingRow(
        int rank,
        String sno,
        String sname,
        int courseCount,
        BigDecimal credits,
        BigDecimal avgScore,
        BigDecimal gpa,
        boolean self) {
}
