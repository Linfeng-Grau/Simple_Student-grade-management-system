<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.score.common.HtmlUtil" %>
<%@ page import="com.example.score.entity.Clazz" %>
<%@ page import="com.example.score.web.view.CourseStudentsView" %>
<%@ page import="com.example.score.web.view.PickableStudent" %>
<%@ page import="com.example.score.web.view.StudentRow" %>
<%@ include file="/WEB-INF/jsp/common/layout-top.jspf" %>
<%-- 学生管理：① 登记学生信息 ② 下拉勾选已登记学生加入课程 ③ 移出本课 / 设班长 / 取消班长 --%>
<%
    List<CourseStudentsView> courses = (List<CourseStudentsView>) request.getAttribute("courses");
    if (courses == null) {
        courses = List.of();
    }
    List<Clazz> classes = (List<Clazz>) request.getAttribute("classes");
    if (classes == null) {
        classes = List.of();
    }
    List<String> errors = (List<String>) request.getAttribute("errors");

    boolean registered = Boolean.TRUE.equals(request.getAttribute("registered"));
    boolean duplicateName = Boolean.TRUE.equals(request.getAttribute("duplicateName"));
    boolean removed = Boolean.TRUE.equals(request.getAttribute("removed"));
    boolean monitorChanged = Boolean.TRUE.equals(request.getAttribute("monitorChanged"));
    Integer enrolledAttr = (Integer) request.getAttribute("enrolledCount");
    int enrolledCount = enrolledAttr == null ? 0 : enrolledAttr;
    Integer skippedAttr = (Integer) request.getAttribute("skippedCount");
    int skippedCount = skippedAttr == null ? 0 : skippedAttr;
    Integer dupAttr = (Integer) request.getAttribute("dupCount");
    int dupCount = dupAttr == null ? 0 : dupAttr;

    List<PickableStudent> pickable = (List<PickableStudent>) request.getAttribute("pickable");
    if (pickable == null) {
        pickable = List.of();
    }

    String formTarget = (String) request.getAttribute("formTarget");
    String regName = (String) request.getAttribute("regName");
    String regSno = (String) request.getAttribute("regSno");
    String regClassNo = (String) request.getAttribute("regClassNo");
    if (regName == null) {
        regName = "";
    }
    if (regSno == null) {
        regSno = "";
    }
    if (regClassNo == null || regClassNo.isEmpty()) {
        regClassNo = classes.isEmpty() ? "" : classes.get(0).getClassNo();
    }
    if (formTarget == null || formTarget.isEmpty()) {
        formTarget = courses.isEmpty() ? "" : courses.get(0).cno();
    }

    int totalRows = 0;
    int totalAvailable = 0;
    for (CourseStudentsView course : courses) {
        totalRows += course.count();
        totalAvailable += course.availableCount();
    }
%>

<section class="stat-grid">
    <div class="stat accent">
        <span class="stat-label">我的课程</span>
        <strong><%= courses.size() %></strong><small>门</small>
        <p class="stat-foot">每门课单独维护学生名单</p>
    </div>
    <div class="stat">
        <span class="stat-label">选课人次</span>
        <strong><%= totalRows %></strong><small>人次</small>
        <p class="stat-foot">同一学生可出现在多门课</p>
    </div>
    <div class="stat ok">
        <span class="stat-label">待加入</span>
        <strong><%= totalAvailable %></strong><small>人</small>
        <p class="stat-foot">本班已登记、还没选本课</p>
    </div>
</section>

<% if (registered) { %>
<div class="alert alert-ok"><span class="alert-icon">✓</span><div>学生信息已登记，现在可以把它加入课程了。</div></div>
<% } %>
<% if (duplicateName) { %>
<div class="alert alert-warn">
    <span class="alert-icon">!</span>
    <div>注意：该班级里已经有同名的同学，请确认这次登记不是重复录入同一个学生。</div>
</div>
<% } %>
<% if (enrolledCount > 0) { %>
<div class="alert alert-ok">
    <span class="alert-icon">✓</span>
    <div>
        已加入 <%= enrolledCount %> 人次课程。
        <% if (dupCount > 0) { %>
        其中 <%= dupCount %> 人次原本就在课程中，已自动忽略。
        <% } %>
        <% if (skippedCount > 0) { %>
        另有 <%= skippedCount %> 名学生因为不属于该班课程而自动跳过。
        <% } %>
    </div>
</div>
<% } %>
<% if (removed) { %>
<div class="alert alert-ok"><span class="alert-icon">✓</span><div>已把该学生移出所选课程（其它课程不受影响）。</div></div>
<% } %>
<% if (monitorChanged) { %>
<div class="alert alert-ok"><span class="alert-icon">✓</span><div>班长设置已更新。</div></div>
<% } %>
<% if (errors != null && !errors.isEmpty()) { %>
<div class="alert alert-error">
    <span class="alert-icon">!</span>
    <div>
        操作未完成：
        <ul>
            <% for (String item : errors) { %>
            <li><%= HtmlUtil.esc(item) %></li>
            <% } %>
        </ul>
        <p class="small" style="margin: 6px 0 0;">
            如果提示「此学生不存在」，请先用下面的「第一步：登记学生信息」把它录入系统，再回来加入课程。
        </p>
    </div>
</div>
<% } %>

<section class="card">
    <div class="card-head">
        <h2>第一步：登记学生信息</h2>
        <span class="muted small">学生信息入库后才能加入课程；初始密码统一为 114514</span>
    </div>

    <% if (classes.isEmpty()) { %>
    <p class="muted">系统中还没有班级数据，无法登记学生。</p>
    <% } else { %>
    <form method="post" action="<%= ctx %>/teacher/students/register" autocomplete="off">
        <div class="toolbar add-form">
            <div class="field grow">
                <label for="regName">姓名</label>
                <input class="input" type="text" id="regName" name="name" maxlength="20" required
                       placeholder="2~20 个中文或英文字符" value="<%= HtmlUtil.esc(regName) %>">
            </div>
            <div class="field grow">
                <label for="regSno">学号</label>
                <input class="input" type="text" id="regSno" name="sno" required
                       inputmode="numeric" pattern="\d{12}" maxlength="12"
                       placeholder="12 位数字，如 202421147014" value="<%= HtmlUtil.esc(regSno) %>">
            </div>
            <div class="field">
                <label for="regClass">所属班级</label>
                <select class="input" id="regClass" name="classNo">
                    <% for (Clazz clazz : classes) { %>
                    <option value="<%= HtmlUtil.esc(clazz.getClassNo()) %>"
                            <%= clazz.getClassNo().equals(regClassNo) ? "selected" : "" %>>
                        <%= HtmlUtil.esc(clazz.getClassName()) %>
                    </option>
                    <% } %>
                </select>
            </div>
            <button class="btn btn-primary" type="submit">登记学生</button>
        </div>
    </form>
    <% } %>
</section>

<section class="card">
    <div class="card-head">
        <h2>第二步：把学生加入课程</h2>
        <span class="muted small">选项来自已登记学生，点开勾选即可（支持多选）</span>
    </div>

    <% if (courses.isEmpty()) { %>
    <p class="muted">当前没有分配给你的课程，无法加入学生。</p>
    <% } else if (pickable.isEmpty()) { %>
    <p class="muted">还没有可加入的学生，请先用上面的「登记学生信息」录入。</p>
    <% } else { %>
    <form method="post" action="<%= ctx %>/teacher/students/add" autocomplete="off">
        <div class="toolbar add-form">
            <div class="field grow">
                <label>选择学生（可多选）</label>
                <details class="picker" id="picker">
                    <summary class="picker-summary" id="pickerSummary">
                        点击选择学生（共 <%= pickable.size() %> 人可选）
                    </summary>
                    <div class="picker-panel">
                        <input class="input" type="search" id="pickerFilter"
                               placeholder="按姓名 / 学号过滤…" autocomplete="off">
                        <div class="picker-list">
                            <% for (PickableStudent item : pickable) { %>
                            <label class="picker-item"
                                   data-key="<%= HtmlUtil.esc(item.sname()) %> <%= HtmlUtil.esc(item.sno()) %> <%= HtmlUtil.esc(item.className()) %>">
                                <input type="checkbox" name="snos" value="<%= HtmlUtil.esc(item.sno()) %>">
                                <span><%= HtmlUtil.esc(item.sname()) %>
                                    · <%= HtmlUtil.esc(item.sno()) %>
                                    · <%= HtmlUtil.esc(item.className()) %></span>
                            </label>
                            <% } %>
                        </div>
                    </div>
                </details>
            </div>
            <div class="field">
                <label for="studentTarget">加入课程</label>
                <select class="input" id="studentTarget" name="target">
                    <% for (CourseStudentsView course : courses) { %>
                    <option value="<%= HtmlUtil.esc(course.cno()) %>"
                            <%= course.cno().equals(formTarget) ? "selected" : "" %>>
                        <%= HtmlUtil.esc(course.cname()) %>（<%= HtmlUtil.esc(course.cno()) %>）
                    </option>
                    <% } %>
                    <option value="ALL" <%= "ALL".equals(formTarget) ? "selected" : "" %>>我的全部课程</option>
                </select>
            </div>
            <button class="btn btn-primary" type="submit">加入课程</button>
        </div>
    </form>
    <p class="muted small" style="margin: 12px 0 0;">
        校验：勾选项全部来自已登记学生；只加入本班课程（选「我的全部课程」时非本班学生自动跳过）；
        已在课程中的会自动忽略。
    </p>
    <% } %>
</section>

<% if (courses.isEmpty()) { %>
<div class="card empty">
    <div class="empty-icon">☷</div>
    <p>暂无可管理的学生。</p>
</div>
<% } %>

<% for (CourseStudentsView course : courses) { %>
<section class="card course-students">
    <div class="card-head">
        <h2><%= HtmlUtil.esc(course.cname()) %></h2>
        <span class="muted small">
            <%= HtmlUtil.esc(course.cno()) %> · <%= HtmlUtil.esc(course.className()) %> · 共 <%= course.count() %> 人
        </span>
    </div>

    <% if (course.students().isEmpty()) { %>
    <p class="muted">这门课还没有学生，可以从下面的「待加入学生」里挑一个加进来。</p>
    <% } else { %>
    <div class="table-wrap">
        <table class="table">
            <thead>
            <tr>
                <th>学号</th>
                <th>姓名</th>
                <th>身份</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <% for (StudentRow student : course.students()) { %>
            <tr>
                <td class="cell-main"><%= HtmlUtil.esc(student.sno()) %></td>
                <td><%= HtmlUtil.esc(student.sname()) %></td>
                <td>
                    <% if (student.monitor()) { %>
                    <span class="monitor-tag">班长</span>
                    <% } else { %>
                    <span class="muted">学生</span>
                    <% } %>
                </td>
                <td>
                    <div class="student-actions">
                        <form method="post" action="<%= ctx %>/teacher/students/monitor">
                            <input type="hidden" name="sno" value="<%= HtmlUtil.esc(student.sno()) %>">
                            <% if (student.monitor()) { %>
                            <input type="hidden" name="action" value="clear">
                            <button class="btn btn-ghost btn-sm" type="submit">取消班长</button>
                            <% } else { %>
                            <input type="hidden" name="action" value="set">
                            <button class="btn btn-ghost btn-sm" type="submit">设为班长</button>
                            <% } %>
                        </form>
                        <form method="post" action="<%= ctx %>/teacher/students/remove"
                              onsubmit="return confirm('从《<%= HtmlUtil.esc(course.cname()) %>》中移出该学生？只影响这门课，成绩会一并删除。');">
                            <input type="hidden" name="cno" value="<%= HtmlUtil.esc(course.cno()) %>">
                            <input type="hidden" name="sno" value="<%= HtmlUtil.esc(student.sno()) %>">
                            <button class="btn btn-ghost btn-sm" type="submit">移出本课</button>
                        </form>
                    </div>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% } %>

    <% if (!course.available().isEmpty()) { %>
    <div class="available-box">
        <span class="muted small">本班已登记、还没选这门课（点一下即可加入）：</span>
        <div class="chip-list">
            <% for (StudentRow student : course.available()) { %>
            <form method="post" action="<%= ctx %>/teacher/students/enroll">
                <input type="hidden" name="cno" value="<%= HtmlUtil.esc(course.cno()) %>">
                <input type="hidden" name="sno" value="<%= HtmlUtil.esc(student.sno()) %>">
                <button class="chip-btn" type="submit">
                    + <%= HtmlUtil.esc(student.sname()) %>（<%= HtmlUtil.esc(student.sno()) %>）
                </button>
            </form>
            <% } %>
        </div>
    </div>
    <% } %>

    <p class="muted small" style="margin: 10px 0 0;">
        删除按课程生效：移出后该生仍保留在其它课程里；班长设置按班级生效，取消后该班可以没有班长。
    </p>
</section>
<% } %>

<%@ include file="/WEB-INF/jsp/common/layout-bottom.jspf" %>
