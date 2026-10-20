<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.score.common.HtmlUtil" %>
<%@ page import="com.example.score.web.view.DemoAccount" %>
<%@ page import="java.util.List" %>
<%-- 登录页：error / username 回显失败信息，redirect 记住被拦截的地址，loggedOut 显示退出提示 --%>
<%
    String ctx = request.getContextPath();
    String error = (String) request.getAttribute("error");
    String username = (String) request.getAttribute("username");
    String redirect = (String) request.getAttribute("redirect");
    boolean loggedOut = Boolean.TRUE.equals(request.getAttribute("loggedOut"));
    List<DemoAccount> demoAccounts = (List<DemoAccount>) request.getAttribute("demoAccounts");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>登录 · 学生成绩管理系统</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body class="login">
<div class="login-shell">
    <aside class="login-aside">
        <div>
            <h1>学生成绩管理系统</h1>
            <p class="tagline">教师录成绩 · 学生查成绩 · 班长看排名</p>
            <ul class="feature-list">
                <li><span class="dot">1</span><span>按课程录入平时与考试成绩，比例可随时调整</span></li>
                <li><span class="dot">2</span><span>总评自动计算，绩点按（总评 − 50）÷ 10 折算</span></li>
                <li><span class="dot">3</span><span>学生查看本人成绩，班长按绩点查看全班排名</span></li>
            </ul>
        </div>
        <p class="aside-foot">Spring Boot · JSP · H2　课程设计演示系统</p>
    </aside>

    <section class="login-card">
        <h2>账号登录</h2>
        <p class="subtitle">输入学号或工号，系统会自动识别你的角色</p>

        <% if (loggedOut) { %>
        <div class="alert alert-ok"><span class="alert-icon">✓</span><div>你已安全退出登录。</div></div>
        <% } %>
        <% if (error != null) { %>
        <div class="alert alert-error"><span class="alert-icon">!</span><div><%= HtmlUtil.esc(error) %></div></div>
        <% } %>

        <form method="post" action="<%= ctx %>/login" autocomplete="off">
            <input type="hidden" name="redirect" value="<%= HtmlUtil.esc(redirect) %>">
            <div class="field">
                <label for="username">学号 / 工号</label>
                <input class="input" type="text" id="username" name="username" required maxlength="40"
                       placeholder="例如：202421147011 或 T1001"
                       value="<%= HtmlUtil.esc(username) %>">
            </div>
            <div class="field">
                <label for="password">密码</label>
                <input class="input" type="password" id="password" name="password" required maxlength="60"
                       placeholder="演示账号 123456；教师新建学生 114514">
            </div>
            <button class="login-submit" type="submit">登 录</button>
        </form>

        <div class="demo-box">
            <p class="demo-title">点一下即可填入演示账号</p>
            <div class="demo-list">
                <button class="demo-btn" type="button" data-fill-user="T1001">教师 T1001</button>
                <% if (demoAccounts != null && !demoAccounts.isEmpty()) {
                       for (DemoAccount account : demoAccounts) { %>
                <button class="demo-btn" type="button"
                        data-role="<%= account.monitor() ? "monitor" : "student" %>"
                        data-fill-user="<%= HtmlUtil.esc(account.username()) %>"><%= account.roleLabel() %> <%= HtmlUtil.esc(account.name()) %></button>
                <%     }
                   } %>
            </div>
            <p class="hint">
                演示账号密码均为 123456（含系统随机选中的班长）；
                教师新增学生的初始密码是 114514，首次登录必须先改成「字母 + 数字」组合。
            </p>
        </div>
    </section>
</div>
<script src="<%= ctx %>/js/app.js"></script>
</body>
</html>
