<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="fragments/header.jsp">
    <jsp:param name="pageTitle" value="My items"/>
</jsp:include>

<h1>My items</h1>

<section>
    <div class="section-head">
        <h2>Reports I posted (${myItems.size()})</h2>
        <a class="btn btn-primary btn-small" href="${ctx}/post">+ Post an item</a>
    </div>
    <c:choose>
        <c:when test="${empty myItems}">
            <p class="empty">You have not posted anything yet.</p>
        </c:when>
        <c:otherwise>
            <table class="table">
                <thead>
                <tr><th>Type</th><th>Item</th><th>Date</th><th>Status</th><th>Pending messages</th></tr>
                </thead>
                <tbody>
                <c:forEach var="item" items="${myItems}">
                    <tr>
                        <td><span class="badge badge-${item.type} badge-inline">${item.type}</span></td>
                        <td><a href="${ctx}/item?id=${item.id}"><c:out value="${item.title}"/></a></td>
                        <td><fmt:formatDate value="${item.eventDate}" pattern="dd MMM yyyy"/></td>
                        <td><span class="badge badge-${item.status} badge-inline">${item.status}</span></td>
                        <td>
                            <c:choose>
                                <c:when test="${item.pendingClaims > 0}"><strong class="alert-count">${item.pendingClaims} waiting</strong></c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</section>

<section>
    <h2>Messages I sent (${myClaims.size()})</h2>
    <c:choose>
        <c:when test="${empty myClaims}">
            <p class="empty">You have not contacted anyone yet. Open an item and use the message box.</p>
        </c:when>
        <c:otherwise>
            <table class="table">
                <thead>
                <tr><th>Item</th><th>My message</th><th>Sent</th><th>Result</th></tr>
                </thead>
                <tbody>
                <c:forEach var="claim" items="${myClaims}">
                    <tr>
                        <td><a href="${ctx}/item?id=${claim.itemId}"><c:out value="${claim.itemTitle}"/></a></td>
                        <td><c:out value="${claim.message}"/></td>
                        <td><fmt:formatDate value="${claim.createdAt}" pattern="dd MMM yyyy"/></td>
                        <td><span class="badge badge-${claim.status} badge-inline">${claim.status}</span></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</section>

<jsp:include page="fragments/footer.jsp"/>
