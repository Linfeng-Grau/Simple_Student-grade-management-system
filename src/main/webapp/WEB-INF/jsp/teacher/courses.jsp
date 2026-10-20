<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.score.common.Fmt" %>
<%@ page import="com.example.score.common.HtmlUtil" %>
<%@ page import="com.example.score.web.view.TeacherCourseView" %>
<%@ include file="/WEB-INF/jsp/common/layout-top.jspf" %>
<%
    List<TeacherCourseView> courses = (List<TeacherCourseView>) request.getAttribute("courses");
    if (courses == null) {
        courses = List.of();
    }
    int totalStudents = 0;
    int totalEntered = 0;
    for (TeacherCourseView item : courses) {
        totalStudents += item.studentCount();
        totalEntered += item.enteredCount();
    }
%>

<section class="stat-grid">
    <div class="stat accent">
        <span class="stat-label">承担课程</span>
        <strong><%= courses.size() %></strong><small>门</small>
        <p class="stat-foot">可在下方任选一门录入成绩</p>
    </div>
    <div class="stat">
        <span class="stat-label">学生人次</span>
        <strong><%= totalStudents %></strong><small>人</small>
        <p class="stat-foot">各课程班级人数合计</p>
    </div>
    <div class="stat ok">
        <span class="stat-label">已录成绩</span>
        <strong><%= totalEntered %></strong><small>条</small>
        <p class="stat-foot">留空保存可撤销误录</p>
    </div>
    <div class="stat">
        <span class="stat-label">整体进度</span>
        <strong><%= totalStudents == 0 ? 0 : Math.round(totalEntered * 100f / totalStudents) %></strong><small>%</small>
        <p class="stat-foot">已录 / 应录</p>
    </div>
</section>

<% if (courses.isEmpty()) { %>
<div class="card empty">
    <div class="empty-icon">▤</div>
    <p>当前没有分配给你的课程。</p>
    <p>请联系教务把课程关联到你的工号（演示数据中教师 T1001~T1003 各有课程）。</p>
</div>
<% } else { %>
<div class="course-grid">
    <% for (TeacherCourseView course : courses) { %>
    <article class="course-card">
        <header>
            <div>
                <h3><%= HtmlUtil.esc(course.cname()) %></h3>
                <p class="course-code"><%= HtmlUtil.esc(course.cno()) %> · <%= HtmlUtil.esc(course.className()) %></p>
            </div>
            <span class="credit"><%= Fmt.dec1(course.credit()) %> 学分</span>
        </header>

        <dl class="meta">
            <div>
                <dt>班级人数</dt>
                <dd><%= course.studentCount() %> 人</dd>
            </div>
            <div>
                <dt>已录人数</dt>
                <dd><%= course.enteredCount() %> 人</dd>
            </div>
            <div>
                <dt>班级平均分</dt>
                <dd><%= Fmt.dec1(course.avgTotal()) %></dd>
            </div>
            <div>
                <dt>平时 / 考试</dt>
                <dd><%= course.usualRatio() %>% / <%= course.examRatio() %>%</dd>
            </div>
        </dl>

        <div class="progress" title="录入进度 <%= course.percent() %>%">
            <span style="width: <%= course.percent() %>%"></span>
        </div>

        <footer>
            <span class="muted small">录入进度 <%= course.percent() %>%</span>
            <a class="btn btn-primary btn-sm" href="<%= ctx %>/teacher/course/<%= HtmlUtil.esc(course.cno()) %>">
                录入 / 修改成绩
            </a>
        </footer>
    </article>
    <% } %>
</div>
<% } %>

<%@ include file="/WEB-INF/jsp/common/layout-bottom.jspf" %>
