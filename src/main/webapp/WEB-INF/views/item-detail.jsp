<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="fragments/header.jsp">
    <jsp:param name="pageTitle" value="${item.title}"/>
</jsp:include>

<p><a href="${ctx}/items">&larr; Back to all items</a></p>

<article class="detail">
    <div class="detail-media">
        <div class="noimg noimg-big">📦</div>
    </div>

    <div class="detail-info">
        <p>
            <span class="badge badge-${item.type} badge-inline">${item.type}</span>
            <span class="badge badge-${item.status} badge-inline">${item.status}</span>
        </p>
        <h1><c:out value="${item.title}"/></h1>

        <dl class="facts">
            <dt>Category</dt><dd><c:out value="${item.category}"/></dd>
            <dt>Place</dt><dd><c:out value="${item.location}"/></dd>
            <dt>Date</dt><dd><fmt:formatDate value="${item.eventDate}" pattern="EEEE, dd MMMM yyyy"/></dd>
            <dt>Posted by</dt><dd><c:out value="${item.ownerName}"/> on <fmt:formatDate value="${item.createdAt}" pattern="dd MMM yyyy"/></dd>
        </dl>

        <c:if test="${not empty item.description}">
            <h3>Description</h3>
            <p class="description"><c:out value="${item.description}"/></p>
        </c:if>

        <%-- ================= What the viewer can do depends on who he/she is ================= --%>
        <c:choose>

            <%-- 1. OWNER of the item --%>
            <c:when test="${isOwner}">
                <div class="panel">
                    <h3>You posted this</h3>
                    <c:if test="${item.status == 'OPEN'}">
                        <form action="${ctx}/manage" method="post" class="inline-form">
                            <input type="hidden" name="itemId" value="${item.id}">
                            <input type="hidden" name="action" value="resolve">
                            <button type="submit" class="btn btn-success">Mark as resolved</button>
                        </form>
                    </c:if>
                    <form action="${ctx}/manage" method="post" class="inline-form"
                          onsubmit="return confirm('Delete this item permanently?');">
                        <input type="hidden" name="itemId" value="${item.id}">
                        <input type="hidden" name="action" value="delete">
                        <button type="submit" class="btn btn-danger">Delete</button>
                    </form>
                </div>

                <div class="panel">
                    <h3>Messages / claims (${claims.size()})</h3>
                    <c:if test="${empty claims}">
                        <p class="muted">Nobody has contacted you about this item yet.</p>
                    </c:if>
                    <c:forEach var="claim" items="${claims}">
                        <div class="claim claim-${claim.status}">
                            <div class="claim-head">
                                <strong><c:out value="${claim.claimerName}"/></strong>
                                <span class="badge badge-${claim.status} badge-inline">${claim.status}</span>
                            </div>
                            <p><c:out value="${claim.message}"/></p>
                            <p class="muted">
                                ✉ <c:out value="${claim.claimerEmail}"/>
                                <c:if test="${not empty claim.claimerPhone}"> &middot; ☎ <c:out value="${claim.claimerPhone}"/></c:if>
                            </p>
                            <c:if test="${claim.status == 'PENDING' && item.status == 'OPEN'}">
                                <form action="${ctx}/decide" method="post" class="inline-form">
                                    <input type="hidden" name="claimId" value="${claim.id}">
                                    <button type="submit" name="decision" value="approve" class="btn btn-success btn-small">Approve</button>
                                    <button type="submit" name="decision" value="reject" class="btn btn-outline btn-small">Reject</button>
                                </form>
                            </c:if>
                        </div>
                    </c:forEach>
                </div>
            </c:when>

            <%-- 2. Logged-in user who is NOT the owner --%>
            <c:when test="${not empty sessionScope.user}">
                <div class="panel">
                    <c:choose>
                        <c:when test="${not empty myClaim}">
                            <h3>Your message</h3>
                            <div class="claim claim-${myClaim.status}">
                                <div class="claim-head">
                                    <span class="badge badge-${myClaim.status} badge-inline">${myClaim.status}</span>
                                </div>
                                <p><c:out value="${myClaim.message}"/></p>
                            </div>
                            <c:if test="${showOwnerContact}">
                                <p class="success-box">
                                    Approved! Contact <strong><c:out value="${item.ownerName}"/></strong>:
                                    ✉ <c:out value="${item.ownerEmail}"/>
                                    <c:if test="${not empty item.ownerPhone}"> &middot; ☎ <c:out value="${item.ownerPhone}"/></c:if>
                                </p>
                            </c:if>
                        </c:when>
                        <c:when test="${item.status == 'OPEN'}">
                            <h3>${item.type == 'FOUND' ? 'Is this yours?' : 'Did you find it?'}</h3>
                            <p class="muted">
                                Send a short message to the owner of this report
                                (${item.type == 'FOUND' ? 'describe something only the real owner would know' : 'tell where the item is now'}).
                                Your e-mail and phone will be shown to them.
                            </p>
                            <form action="${ctx}/claim" method="post">
                                <input type="hidden" name="itemId" value="${item.id}">
                                <textarea name="message" rows="4" maxlength="500" required
                                          placeholder="Write your message here..."></textarea>
                                <button type="submit" class="btn btn-primary">Send message</button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <p class="muted">This item has been resolved.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:when>

            <%-- 3. Visitor who is not logged in --%>
            <c:otherwise>
                <div class="panel">
                    <p><a href="${ctx}/login">Log in</a> or <a href="${ctx}/register">sign up</a>
                       to contact the person who posted this.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</article>

<jsp:include page="fragments/footer.jsp"/>
