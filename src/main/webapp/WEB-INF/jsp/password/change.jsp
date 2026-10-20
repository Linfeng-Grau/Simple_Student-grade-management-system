<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.score.common.HtmlUtil" %>
<%@ page import="com.example.score.common.LoginUser" %>
<%@ page import="com.example.score.common.SessionUtil" %>
<%-- 修改密码页：mustChange 提示首次登录强制改密，error 回显校验失败原因 --%>
<%
    String ctx = request.getContextPath();
    LoginUser pwdUser = SessionUtil.get(session);
    String error = (String) request.getAttribute("error");
    boolean mustChange = Boolean.TRUE.equals(request.getAttribute("mustChange"));
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>修改密码 · 学生成绩管理系统</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body class="login">
<div class="password-card">
    <h2>修改密码</h2>
    <p class="subtitle">
        <% if (pwdUser != null) { %>
        <%= HtmlUtil.esc(pwdUser.getName()) %>（<%= HtmlUtil.esc(pwdUser.getUsername()) %>）· <%= pwdUser.getRole().getLabel() %>
        <% } %>
    </p>

    <% if (mustChange) { %>
    <div class="alert alert-warn">
        <span class="alert-icon">!</span>
        <div>这是教师新建的账号（初始密码 114514），请先设置自己的密码后继续使用。</div>
    </div>
    <% } %>
    <% if (error != null) { %>
    <div class="alert alert-error"><span class="alert-icon">!</span><div><%= HtmlUtil.esc(error) %></div></div>
    <% } %>

    <form method="post" action="<%= ctx %>/password/change" autocomplete="off">
        <div class="field">
            <label for="newPassword">新密码</label>
            <input class="input" type="password" id="newPassword" name="newPassword" required
                   minlength="8" maxlength="20" placeholder="8~20 位，必须同时包含字母和数字">
        </div>
        <div class="field">
            <label for="confirmPassword">确认新密码</label>
            <input class="input" type="password" id="confirmPassword" name="confirmPassword" required
                   minlength="8" maxlength="20" placeholder="再输入一次新密码">
        </div>
        <button class="login-submit" type="submit">保存新密码</button>
    </form>

    <div class="actions">
        <span class="muted small">8~20 位，必须包含字母和数字，且不能使用初始密码</span>
        <a class="btn btn-ghost btn-sm" href="<%= ctx %>/logout">退出登录</a>
    </div>
</div>
</body>
</html>
