<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="fragments/header.jsp">
    <jsp:param name="pageTitle" value="Home"/>
</jsp:include>

<section class="hero">
    <h1>Lost something on campus?<br>Found something that isn't yours?</h1>
    <p>Post it here. Students can search, send a message to the owner, and get lost things back.</p>
    <div class="hero-actions">
        <a class="btn btn-primary" href="${ctx}/post?type=LOST">I lost something</a>
        <a class="btn btn-success" href="${ctx}/post?type=FOUND">I found something</a>
        <a class="btn btn-outline" href="${ctx}/items">Browse all items</a>
    </div>
</section>

<section class="stats">
    <div class="stat"><span class="stat-num">${lostCount}</span><span class="stat-label">Open lost reports</span></div>
    <div class="stat"><span class="stat-num">${foundCount}</span><span class="stat-label">Open found reports</span></div>
    <div class="stat"><span class="stat-num">${resolvedCount}</span><span class="stat-label">Items reunited</span></div>
</section>

<section>
    <div class="section-head">
        <h2>Latest reports</h2>
        <a href="${ctx}/items">See all &rarr;</a>
    </div>
    <c:choose>
        <c:when test="${empty recent}">
            <p class="empty">Nothing has been posted yet. Be the first!</p>
        </c:when>
        <c:otherwise>
            <div class="grid">
                <c:forEach var="item" items="${recent}">
                    <%@ include file="/WEB-INF/views/fragments/item-card.jspf" %>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<jsp:include page="fragments/footer.jsp"/>
