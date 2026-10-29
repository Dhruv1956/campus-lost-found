<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="fragments/header.jsp">
    <jsp:param name="pageTitle" value="Sign up"/>
</jsp:include>

<div class="form-card">
    <h1>Create an account</h1>

    <c:if test="${not empty errors}">
        <div class="flash flash-error">
            <ul>
                <c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach>
            </ul>
        </div>
    </c:if>

    <form action="${ctx}/register" method="post">
        <label for="fullName">Full name</label>
        <input type="text" id="fullName" name="fullName" required maxlength="100" value="<c:out value='${param.fullName}'/>">

        <label for="email">E-mail</label>
        <input type="email" id="email" name="email" required maxlength="120" value="<c:out value='${param.email}'/>">

        <label for="phone">Phone (optional - shown only to people you approve)</label>
        <input type="text" id="phone" name="phone" maxlength="15" value="<c:out value='${param.phone}'/>">

        <label for="password">Password (min 6 characters)</label>
        <input type="password" id="password" name="password" required minlength="6">

        <label for="confirm">Confirm password</label>
        <input type="password" id="confirm" name="confirm" required minlength="6">

        <button type="submit" class="btn btn-primary btn-block">Sign up</button>
    </form>

    <p class="muted center">Already registered? <a href="${ctx}/login">Login</a></p>
</div>

<jsp:include page="fragments/footer.jsp"/>
