<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="fragments/header.jsp">
    <jsp:param name="pageTitle" value="Oops"/>
</jsp:include>

<div class="form-card center">
    <h1>
        <c:choose>
            <c:when test="${pageContext.errorData.statusCode == 404}">Page not found</c:when>
            <c:when test="${pageContext.errorData.statusCode == 403}">Not allowed</c:when>
            <c:otherwise>Something went wrong</c:otherwise>
        </c:choose>
    </h1>
    <p class="muted">
        <c:choose>
            <c:when test="${pageContext.errorData.statusCode == 404}">The page or item you asked for does not exist.</c:when>
            <c:when test="${pageContext.errorData.statusCode == 403}">You do not have permission to do that.</c:when>
            <c:otherwise>An unexpected error occurred. Please try again.</c:otherwise>
        </c:choose>
    </p>
    <a class="btn btn-primary" href="${ctx}/">Back to home</a>
</div>

<jsp:include page="fragments/footer.jsp"/>
