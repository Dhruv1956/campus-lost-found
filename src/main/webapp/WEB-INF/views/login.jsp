<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="fragments/header.jsp">
    <jsp:param name="pageTitle" value="Login"/>
</jsp:include>

<div class="form-card">
    <h1>Login</h1>

    <c:if test="${not empty error}">
        <div class="flash flash-error"><c:out value="${error}"/></div>
    </c:if>

    <form action="${ctx}/login" method="post">
        <label for="email">E-mail</label>
        <input type="email" id="email" name="email" required value="<c:out value='${param.email}'/>">

        <label for="password">Password</label>
        <input type="password" id="password" name="password" required>

        <button type="submit" class="btn btn-primary btn-block">Login</button>
    </form>

    <p class="muted center">New here? <a href="${ctx}/register">Create an account</a></p>
    <p class="muted center demo-hint">Demo account: <code>demo@campus.edu</code> / <code>demo123</code></p>
</div>

<jsp:include page="fragments/footer.jsp"/>
