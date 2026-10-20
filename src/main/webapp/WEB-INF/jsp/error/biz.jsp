<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.score.common.HtmlUtil" %>
<%@ page import="com.example.score.common.LoginUser" %>
<%@ page import="com.example.score.common.SessionUtil" %>
<%
    String ctx = request.getContextPath();
    String errorTitle = (String) request.getAttribute("errorTitle");
    String errorMessage = (String) request.getAttribute("errorMessage");
    LoginUser errorUser = SessionUtil.get(session);
    if (errorTitle == null) {
        errorTitle = "操作未完成";
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><%= HtmlUtil.esc(errorTitle) %> · 学生成绩管理系统</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body class="simple">
<div class="error-card">
    <div class="error-code">!</div>
    <h1><%= HtmlUtil.esc(errorTitle) %></h1>
    <p><%= HtmlUtil.esc(errorMessage) %></p>
    <div class="actions">
        <% if (errorUser != null) { %>
        <a class="btn btn-primary" href="<%= ctx %><%= errorUser.getRole().getHomePath() %>">返回我的首页</a>
        <a class="btn btn-ghost" href="<%= ctx %>/logout">切换账号</a>
        <% } else { %>
        <a class="btn btn-primary" href="<%= ctx %>/login">去登录</a>
        <% } %>
    </div>
</div>
</body>
</html>
