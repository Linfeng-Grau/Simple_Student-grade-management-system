package com.example.score.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 页面数字格式化：统一小数位，「未录入」统一显示破折号，空值不输出 null。
 */
public final class Fmt {

    private Fmt() {
    }

    /** 保留 1 位小数，空值显示破折号 */
    public static String dec1(BigDecimal value) {
        return value == null ? "—" : value.setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    /** 保留 2 位小数，空值显示破折号 */
    public static String dec2(BigDecimal value) {
        return value == null ? "—" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** 用于 input 的 value：空值输出空串 */
    public static String raw(BigDecimal value) {
        return value == null ? "" : value.setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    /** 百分比展示，空值显示破折号 */
    public static String percent(BigDecimal value) {
        return value == null ? "—" : value.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    /** 分数对应的样式类：good（≥80）/ pass（60~79.9）/ fail（<60）/ none（未录入）。 */
    public static String level(BigDecimal total) {
        if (total == null) {
            return "none";
        }
        if (total.compareTo(new BigDecimal("80")) >= 0) {
            return "good";
        }
        if (total.compareTo(new BigDecimal("60")) >= 0) {
            return "pass";
        }
        return "fail";
    }

    /** 分数对应的中文评语 */
    public static String levelText(BigDecimal total) {
        return switch (level(total)) {
            case "good" -> "优秀";
            case "pass" -> "及格";
            case "fail" -> "不及格";
            default -> "未录入";
        };
    }
}
