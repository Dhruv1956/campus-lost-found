<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
    Shared page header: <head>, navigation bar and the flash message.
    Every page includes it with:  <jsp:include page="fragments/header.jsp"><jsp:param name="pageTitle" value="..."/></jsp:include>
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${param.pageTitle}"/> | CampusFind</title>
    <link rel="stylesheet" href="${ctx}/css/style.css">
</head>
<body>
<header class="topbar">
    <div class="container topbar-inner">
        <a class="brand" href="${ctx}/">🎒 Campus<span>Find</span></a>
        <nav class="nav">
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <a href="${ctx}/">Home</a>
                    <a href="${ctx}/items">Browse</a>
                    <a href="${ctx}/post" class="nav-cta">+ Post an item</a>
                    <a href="${ctx}/my-items">My items</a>
                    <form action="${ctx}/logout" method="post" class="inline-form">
                        <span class="hello">Hi, <c:out value="${sessionScope.user.fullName}"/></span>
                        <button type="submit" class="link-button">Logout</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <a href="${ctx}/login">Login</a>
                    <a href="${ctx}/register" class="nav-cta">Sign up</a>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>
</header>

<main class="container">
    <%-- Flash message: shown once, then removed from the session --%>
    <c:if test="${not empty sessionScope.flash}">
        <div class="flash flash-${sessionScope.flashType}"><c:out value="${sessionScope.flash}"/></div>
        <c:remove var="flash" scope="session"/>
        <c:remove var="flashType" scope="session"/>
    </c:if>
