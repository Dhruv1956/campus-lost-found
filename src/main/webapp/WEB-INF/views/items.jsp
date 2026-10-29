<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="fragments/header.jsp">
    <jsp:param name="pageTitle" value="Browse items"/>
</jsp:include>

<h1>Browse items</h1>

<%-- Search form: a GET form, so the filters appear in the URL (/items?q=...&type=...) --%>
<form action="${ctx}/items" method="get" class="filter-bar">
    <input type="text" name="q" placeholder="Search title, description, place..." value="<c:out value='${param.q}'/>">

    <select name="type">
        <option value="">Lost &amp; Found</option>
        <option value="LOST"  ${param.type == 'LOST'  ? 'selected' : ''}>Lost only</option>
        <option value="FOUND" ${param.type == 'FOUND' ? 'selected' : ''}>Found only</option>
    </select>

    <select name="category">
        <option value="">All categories</option>
        <c:forEach var="cat" items="${categories}">
            <option value="<c:out value='${cat}'/>" ${param.category == cat ? 'selected' : ''}><c:out value="${cat}"/></option>
        </c:forEach>
    </select>

    <select name="status">
        <option value="OPEN"     ${param.status == 'OPEN' || empty param.status ? 'selected' : ''}>Open</option>
        <option value="RESOLVED" ${param.status == 'RESOLVED' ? 'selected' : ''}>Resolved</option>
        <option value="ALL"      ${param.status == 'ALL' ? 'selected' : ''}>All</option>
    </select>

    <button type="submit" class="btn btn-primary">Search</button>
    <a href="${ctx}/items" class="btn btn-outline">Reset</a>
</form>

<p class="result-count">${items.size()} result(s)</p>

<c:choose>
    <c:when test="${empty items}">
        <p class="empty">No items match your search. Try different words or clear the filters.</p>
    </c:when>
    <c:otherwise>
        <div class="grid">
            <c:forEach var="item" items="${items}">
                <%@ include file="/WEB-INF/views/fragments/item-card.jspf" %>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="fragments/footer.jsp"/>
