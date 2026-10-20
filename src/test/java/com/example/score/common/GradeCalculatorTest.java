package com.example.score.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 成绩口径的单元测试：保证总评、绩点、加权平均与页面/数据库一致。
 */
class GradeCalculatorTest {

    @Test
    @DisplayName("30% 平时 + 70% 考试：80 / 90 → 87.0")
    void computesTotalWithUsualRatio30() {
        BigDecimal total = GradeCalculator.computeTotal(new BigDecimal("80"), new BigDecimal("90"), 30);
        assertEquals(new BigDecimal("87.0"), total);
    }

    @Test
    @DisplayName("50% / 50%：80 / 90 → 85.0")
    void computesTotalWithRatio50() {
        BigDecimal total = GradeCalculator.computeTotal(new BigDecimal("80"), new BigDecimal("90"), 50);
        assertEquals(new BigDecimal("85.0"), total);
    }

    @Test
    @DisplayName("总评保留 1 位小数且四舍五入：70.25 → 70.3")
    void roundsTotalHalfUp() {
        BigDecimal total = GradeCalculator.computeTotal(new BigDecimal("73.4"), new BigDecimal("68.9"), 30);
        assertEquals(new BigDecimal("70.3"), total);
    }

    @Test
    @DisplayName("缺少任一项成绩时不生成总评")
    void returnsNullWhenScoreMissing() {
        assertNull(GradeCalculator.computeTotal(null, new BigDecimal("90"), 30));
        assertNull(GradeCalculator.computeTotal(new BigDecimal("80"), null, 30));
    }

    @Test
    @DisplayName("绩点边界：59.9→0，60→1.00，87→3.70，100→5.00")
    void computesGradePoint() {
        assertEquals(0, new BigDecimal("0.00").compareTo(GradeCalculator.gradePoint(new BigDecimal("59.9"))));
        assertEquals(0, new BigDecimal("1.00").compareTo(GradeCalculator.gradePoint(new BigDecimal("60"))));
        assertEquals(0, new BigDecimal("3.70").compareTo(GradeCalculator.gradePoint(new BigDecimal("87.0"))));
        assertEquals(0, new BigDecimal("5.00").compareTo(GradeCalculator.gradePoint(new BigDecimal("100"))));
    }

    @Test
    @DisplayName("加权平均按学分计算：(90×4 + 80×2) ÷ 6 = 86.67")
    void computesWeightedAverage() {
        BigDecimal average = GradeCalculator.weightedAverage(
                List.of(new BigDecimal("90"), new BigDecimal("80")),
                List.of(new BigDecimal("4.0"), new BigDecimal("2.0")));
        assertEquals(new BigDecimal("86.67"), average);
    }

    @Test
    @DisplayName("没有成绩或学分为 0 时加权平均为 null")
    void weightedAverageHandlesEmptyInput() {
        assertNull(GradeCalculator.weightedAverage(List.of(), List.of()));
        assertNull(GradeCalculator.weightedAverage(
                List.of(new BigDecimal("80")),
                List.of(BigDecimal.ZERO)));
    }

    @Test
    @DisplayName("成绩合法区间为 0~100")
    void validatesScoreRange() {
        assertTrue(GradeCalculator.isValidScore(new BigDecimal("0")));
        assertTrue(GradeCalculator.isValidScore(new BigDecimal("100")));
        assertFalse(GradeCalculator.isValidScore(new BigDecimal("-0.1")));
        assertFalse(GradeCalculator.isValidScore(new BigDecimal("100.1")));
        assertFalse(GradeCalculator.isValidScore(null));
    }

    @Test
    @DisplayName("最高分、最低分、平均分忽略空值")
    void aggregatesIgnoreNulls() {
        List<BigDecimal> values = java.util.Arrays.asList(new BigDecimal("70"), null, new BigDecimal("95"));
        assertEquals(0, new BigDecimal("95").compareTo(GradeCalculator.max(values)));
        assertEquals(0, new BigDecimal("70").compareTo(GradeCalculator.min(values)));
        assertEquals(0, new BigDecimal("82.50").compareTo(GradeCalculator.average(values)));
    }
}
