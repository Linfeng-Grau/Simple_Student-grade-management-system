<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.score.common.Fmt" %>
<%@ page import="com.example.score.common.HtmlUtil" %>
<%@ page import="com.example.score.entity.Student" %>
<%@ page import="com.example.score.web.view.StudentScoreView" %>
<%@ page import="com.example.score.web.view.StudentSummary" %>
<%@ include file="/WEB-INF/jsp/common/layout-top.jspf" %>
<%
    Student studentInfo = (Student) request.getAttribute("student");
    String classLabel = (String) request.getAttribute("className");
    List<StudentScoreView> scoreRows = (List<StudentScoreView>) request.getAttribute("rows");
    StudentSummary sum = (StudentSummary) request.getAttribute("summary");
    boolean monitorView = Boolean.TRUE.equals(request.getAttribute("monitorView"));
%>

<section class="course-hero">
    <div>
        <h2><%= HtmlUtil.esc(studentInfo.getSname()) %></h2>
        <div class="hero-meta">
            <span class="tag">学号 <%= HtmlUtil.esc(studentInfo.getSno()) %></span>
            <span class="tag"><%= HtmlUtil.esc(classLabel) %></span>
            <span class="tag"><%= sum.courseCount() %> 门课程</span>
        </div>
    </div>
    <% if (monitorView) { %>
    <div class="hero-right">
        <a class="btn btn-primary btn-sm" href="<%= ctx %>/monitor/ranking">查看班级排名</a>
    </div>
    <% } %>
</section>

<section class="stat-grid">
    <div class="stat accent">
        <span class="stat-label">加权平均绩点</span>
        <strong><%= Fmt.dec2(sum.weightedGpa()) %></strong>
        <p class="stat-foot">按学分加权 · 绩点 =（总评 − 50）÷ 10</p>
    </div>
    <div class="stat">
        <span class="stat-label">加权平均分</span>
        <strong><%= Fmt.dec1(sum.weightedAvg()) %></strong>
        <p class="stat-foot">仅统计已录入的课程</p>
    </div>
    <div class="stat ok">
        <span class="stat-label">已修学分</span>
        <strong><%= Fmt.dec1(sum.earnedCredits()) %></strong><small>/ <%= Fmt.dec1(sum.planCredits()) %></small>
        <p class="stat-foot">及格课程才计入已修学分</p>
    </div>
    <div class="stat <%= sum.failedCount() > 0 ? "bad" : "" %>">
        <span class="stat-label">已录 / 不及格</span>
        <strong><%= sum.recordedCount() %></strong><small>/ <%= sum.failedCount() %></small>
        <p class="stat-foot">共 <%= sum.courseCount() %> 门课程</p>
    </div>
</section>

<div class="card">
    <div class="card-head">
        <h2>成绩明细</h2>
        <span class="muted">总评由任课教师录入的比例实时计算</span>
    </div>

    <% if (scoreRows == null || scoreRows.isEmpty()) { %>
    <div class="empty">
        <div class="empty-icon">▤</div>
        <p>本班暂未开设课程。</p>
    </div>
    <% } else { %>
    <div class="table-wrap">
        <table class="table">
            <thead>
            <tr>
                <th>课程</th>
                <th class="num">学分</th>
                <th class="num">平时成绩</th>
                <th class="num">考试成绩</th>
                <th class="num">总评</th>
                <th class="num">绩点</th>
                <th>状态</th>
            </tr>
            </thead>
            <tbody>
            <% for (StudentScoreView row : scoreRows) { %>
            <tr>
                <td>
                    <span class="cell-main"><%= HtmlUtil.esc(row.cname()) %></span>
                    <span class="cell-sub"><%= HtmlUtil.esc(row.cno()) %></span>
                </td>
                <td class="num"><%= Fmt.dec1(row.credit()) %></td>
                <td class="num">
                    <%= Fmt.dec1(row.usualScore()) %>
                    <span class="cell-sub">占比 <%= row.usualRatio() %>%</span>
                </td>
                <td class="num">
                    <%= Fmt.dec1(row.examScore()) %>
                    <span class="cell-sub">占比 <%= row.examRatio() %>%</span>
                </td>
                <td class="num">
                    <span class="score-pill <%= Fmt.level(row.totalScore()) %>"><%= Fmt.dec1(row.totalScore()) %></span>
                </td>
                <td class="num"><span class="gpa"><%= Fmt.dec2(row.gradePoint()) %></span></td>
                <td class="muted"><%= Fmt.levelText(row.totalScore()) %></td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>
</div>

<p class="muted small" style="margin-top:12px">
    说明：成绩由任课教师在录入时设定平时/考试比例并自动生成总评；未录入的课程显示为「—」。
</p>

<%@ include file="/WEB-INF/jsp/common/layout-bottom.jspf" %>
