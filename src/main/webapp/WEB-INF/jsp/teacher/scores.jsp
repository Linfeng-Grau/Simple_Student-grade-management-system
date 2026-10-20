<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.example.score.common.Fmt" %>
<%@ page import="com.example.score.common.HtmlUtil" %>
<%@ page import="com.example.score.web.view.CourseEntryPage" %>
<%@ page import="com.example.score.web.view.ScoreRowView" %>
<%@ include file="/WEB-INF/jsp/common/layout-top.jspf" %>
<%
    CourseEntryPage entryPage = (CourseEntryPage) request.getAttribute("page");
    List<String> errorList = (List<String>) request.getAttribute("errors");
    Map<String, String> rawValues = (Map<String, String>) request.getAttribute("rawScores");
    String usualRatioValue = (String) request.getAttribute("usualRatioRaw");
    String examRatioValue = (String) request.getAttribute("examRatioRaw");
    if (usualRatioValue == null) {
        usualRatioValue = String.valueOf(entryPage.usualRatio());
    }
    if (examRatioValue == null) {
        examRatioValue = String.valueOf(entryPage.examRatio());
    }
    boolean saved = Boolean.TRUE.equals(request.getAttribute("saved"));
%>

<section class="course-hero">
    <div>
        <h2><%= HtmlUtil.esc(entryPage.cname()) %></h2>
        <div class="hero-meta">
            <span class="tag">课程号 <%= HtmlUtil.esc(entryPage.cno()) %></span>
            <span class="tag"><%= HtmlUtil.esc(entryPage.className()) %></span>
            <span class="tag"><%= Fmt.dec1(entryPage.credit()) %> 学分</span>
            <span class="tag"><%= entryPage.rows().size() %> 名学生</span>
        </div>
    </div>
    <div class="hero-right">
        已录入 <strong><%= entryPage.enteredCount() %></strong> / <%= entryPage.rows().size() %> 人<br>
        班级平均 <strong><%= Fmt.dec1(entryPage.avgTotal()) %></strong> 分
    </div>
</section>

<% if (saved) { %>
<div class="alert alert-ok">
    <span class="alert-icon">✓</span>
    <div>成绩已保存，总评已按当前比例重新计算。</div>
</div>
<% } %>

<% if (errorList != null && !errorList.isEmpty()) { %>
<div class="alert alert-error">
    <span class="alert-icon">!</span>
    <div>
        <strong>没有保存，请先修正以下问题：</strong>
        <ul>
            <% for (String message : errorList) { %>
            <li><%= HtmlUtil.esc(message) %></li>
            <% } %>
        </ul>
    </div>
</div>
<% } %>

<div class="alert alert-error" id="clientError" hidden></div>

<form id="scoreForm" method="post" action="<%= ctx %>/teacher/course/<%= HtmlUtil.esc(entryPage.cno()) %>" autocomplete="off">

    <section class="ratio-panel">
        <div class="ratio-field">
            <label for="usualRatio">平时成绩所占比例</label>
            <div class="ratio-line">
                <input class="input input-sm" type="number" id="usualRatio" name="usualRatio"
                       min="0" max="100" step="1" value="<%= HtmlUtil.esc(usualRatioValue) %>">
                <span class="unit">%</span>
                <input type="range" id="usualRange" min="0" max="100" step="1"
                       value="<%= HtmlUtil.esc(usualRatioValue) %>" aria-label="拖动调整平时成绩比例">
            </div>
        </div>
        <div class="ratio-field">
            <label for="examRatio">考试成绩所占比例</label>
            <div class="ratio-line">
                <input class="input input-sm" type="number" id="examRatio" name="examRatio"
                       min="0" max="100" step="1" value="<%= HtmlUtil.esc(examRatioValue) %>">
                <span class="unit">%</span>
            </div>
        </div>
        <p class="hint">
            两项之和必须为 100%，修改任一项另一项会自动补足；总评 = 平时 × 平时比例 + 考试 × 考试比例。
            本次比例保存后对整门课生效。
        </p>
    </section>

    <div class="table-wrap">
        <table class="table">
            <thead>
            <tr>
                <th style="width:52px">#</th>
                <th>学号</th>
                <th>姓名</th>
                <th class="num" style="width:130px">平时成绩</th>
                <th class="num" style="width:130px">考试成绩</th>
                <th class="num" style="width:96px">总评</th>
                <th style="width:96px">等级</th>
            </tr>
            </thead>
            <tbody>
            <%
                int rowIndex = 0;
                for (ScoreRowView row : entryPage.rows()) {
                    rowIndex++;
                    String usualKey = "usual_" + row.sno();
                    String examKey = "exam_" + row.sno();
                    String usualValue = (rawValues != null && rawValues.containsKey(usualKey))
                            ? rawValues.get(usualKey) : Fmt.raw(row.usualScore());
                    String examValue = (rawValues != null && rawValues.containsKey(examKey))
                            ? rawValues.get(examKey) : Fmt.raw(row.examScore());
            %>
            <tr data-sno="<%= HtmlUtil.esc(row.sno()) %>" data-name="<%= HtmlUtil.esc(row.sname()) %>">
                <td class="muted num"><%= rowIndex %></td>
                <td class="num"><span class="cell-main"><%= HtmlUtil.esc(row.sno()) %></span></td>
                <td><%= HtmlUtil.esc(row.sname()) %></td>
                <td class="num">
                    <input class="score-input js-usual" type="text" inputmode="decimal"
                           name="<%= HtmlUtil.esc(usualKey) %>" value="<%= HtmlUtil.esc(usualValue) %>"
                           placeholder="0~100" aria-label="<%= HtmlUtil.esc(row.sname()) %>的平时成绩">
                </td>
                <td class="num">
                    <input class="score-input js-exam" type="text" inputmode="decimal"
                           name="<%= HtmlUtil.esc(examKey) %>" value="<%= HtmlUtil.esc(examValue) %>"
                           placeholder="0~100" aria-label="<%= HtmlUtil.esc(row.sname()) %>的考试成绩">
                </td>
                <td class="num"><span class="total-cell js-total"><%= Fmt.dec1(row.totalScore()) %></span></td>
                <td class="js-level">
                    <span class="score-pill <%= Fmt.level(row.totalScore()) %>"><%= Fmt.levelText(row.totalScore()) %></span>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </div>

    <div class="save-bar">
        <div class="save-stats">
            <span>已录 <b id="filledCount"><%= entryPage.enteredCount() %> / <%= entryPage.rows().size() %></b></span>
            <span>平均 <b><%= Fmt.dec1(entryPage.avgTotal()) %></b></span>
            <span>最高 <b><%= Fmt.dec1(entryPage.maxTotal()) %></b></span>
            <span>最低 <b><%= Fmt.dec1(entryPage.minTotal()) %></b></span>
            <span>及格率 <b><%= Fmt.percent(entryPage.passRate()) %></b></span>
        </div>
        <span class="dirty-tip">有修改尚未保存</span>
        <div class="save-actions">
            <a class="btn btn-ghost" href="<%= ctx %>/teacher/course/<%= HtmlUtil.esc(entryPage.cno()) %>">放弃修改</a>
            <button class="btn btn-primary" type="submit">保存成绩</button>
        </div>
    </div>

    <p class="muted small" style="margin-top:10px">
        提示：平时与考试都留空表示该生尚未录入，保存时会清除其原有成绩；只填一项会被判定为不完整而整批不保存。
    </p>
</form>

<%@ include file="/WEB-INF/jsp/common/layout-bottom.jspf" %>
