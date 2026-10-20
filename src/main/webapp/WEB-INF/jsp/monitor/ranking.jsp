<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.score.common.Fmt" %>
<%@ page import="com.example.score.common.HtmlUtil" %>
<%@ page import="com.example.score.web.view.RankingRow" %>
<%@ page import="com.example.score.web.view.RankingView" %>
<%@ include file="/WEB-INF/jsp/common/layout-top.jspf" %>
<%
    RankingView ranking = (RankingView) request.getAttribute("ranking");
    List<RankingRow> rankingRows = ranking.rows();
%>

<section class="course-hero">
    <div>
        <h2><%= HtmlUtil.esc(ranking.className()) %></h2>
        <div class="hero-meta">
            <span class="tag">班级号 <%= HtmlUtil.esc(ranking.classNo()) %></span>
            <span class="tag"><%= ranking.studentCount() %> 名学生</span>
            <span class="tag"><%= ranking.recordedCount() %> 人已有成绩</span>
        </div>
    </div>
    <div class="hero-right">
        排序规则：学分加权绩点 ↓（同分比加权均分）
    </div>
</section>

<section class="stat-grid">
    <div class="stat accent">
        <span class="stat-label">班级平均绩点</span>
        <strong><%= Fmt.dec2(ranking.classAvgGpa()) %></strong>
        <p class="stat-foot">仅统计已有成绩的同学</p>
    </div>
    <div class="stat ok">
        <span class="stat-label">及格人数</span>
        <strong><%= ranking.passCount() %></strong><small>/ <%= ranking.recordedCount() %></small>
        <p class="stat-foot">加权平均分达到 60 分</p>
    </div>
    <div class="stat">
        <span class="stat-label">及格率</span>
        <strong><%= Fmt.percent(ranking.passRate()) %></strong>
        <p class="stat-foot">及格人数 ÷ 已有成绩人数</p>
    </div>
    <div class="stat">
        <span class="stat-label">未出成绩</span>
        <strong><%= ranking.studentCount() - ranking.recordedCount() %></strong><small>人</small>
        <p class="stat-foot">不参与排名，显示为「—」</p>
    </div>
</section>

<div class="card">
    <div class="card-head">
        <h2>班级成绩排名</h2>
        <span class="muted">数据随教师录入实时变化</span>
    </div>

    <% if (rankingRows.isEmpty()) { %>
    <div class="empty">
        <div class="empty-icon">★</div>
        <p>本班还没有学生信息。</p>
    </div>
    <% } else { %>
    <div class="table-wrap">
        <table class="table">
            <thead>
            <tr>
                <th style="width:64px">名次</th>
                <th>学号</th>
                <th>姓名</th>
                <th class="num">已录课程</th>
                <th class="num">学分</th>
                <th class="num">加权均分</th>
                <th class="num">加权绩点</th>
                <th>备注</th>
            </tr>
            </thead>
            <tbody>
            <% for (RankingRow row : rankingRows) {
                String rankClass = "rank";
                if (row.rank() == 1) {
                    rankClass = "rank rank-1";
                } else if (row.rank() == 2) {
                    rankClass = "rank rank-2";
                } else if (row.rank() == 3) {
                    rankClass = "rank rank-3";
                } else if (row.rank() == 0) {
                    rankClass = "rank rank-none";
                }
            %>
            <tr class="<%= row.self() ? "is-self" : "" %>">
                <td>
                    <span class="<%= rankClass %>"><%= row.rank() == 0 ? "—" : String.valueOf(row.rank()) %></span>
                </td>
                <td class="num"><%= HtmlUtil.esc(row.sno()) %></td>
                <td>
                    <span class="cell-main"><%= HtmlUtil.esc(row.sname()) %></span>
                    <% if (row.self()) { %><span class="self-tag">我</span><% } %>
                </td>
                <td class="num"><%= row.courseCount() %></td>
                <td class="num"><%= Fmt.dec1(row.credits()) %></td>
                <td class="num">
                    <span class="score-pill <%= Fmt.level(row.avgScore()) %>"><%= Fmt.dec1(row.avgScore()) %></span>
                </td>
                <td class="num"><span class="gpa"><%= Fmt.dec2(row.gpa()) %></span></td>
                <td class="muted">
                    <% if (row.courseCount() == 0) { %>暂无成绩<% } else if (row.rank() <= 3) { %>班级前三<% } else { %>—<% } %>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>
</div>

<p class="muted small" style="margin-top:12px">
    说明：绩点按每门课程计算后按学分加权；「加权均分」为各课程总评按学分的加权平均。只有班长可以查看本班排名，其他学生只能查看本人成绩。
</p>

<%@ include file="/WEB-INF/jsp/common/layout-bottom.jspf" %>
