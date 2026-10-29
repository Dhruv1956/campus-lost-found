<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="fragments/header.jsp">
    <jsp:param name="pageTitle" value="Post an item"/>
</jsp:include>

<div class="form-card form-card-wide">
    <h1>Post an item</h1>

    <c:if test="${not empty errors}">
        <div class="flash flash-error">
            <ul>
                <c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach>
            </ul>
        </div>
    </c:if>

    <form action="${ctx}/post" method="post">

        <label>What happened?</label>
        <div class="radio-row">
            <label class="radio"><input type="radio" name="type" value="LOST"
                   ${param.type == 'LOST' || empty param.type ? 'checked' : ''}> I <strong>lost</strong> something</label>
            <label class="radio"><input type="radio" name="type" value="FOUND"
                   ${param.type == 'FOUND' ? 'checked' : ''}> I <strong>found</strong> something</label>
        </div>

        <label for="title">Item name</label>
        <input type="text" id="title" name="title" required minlength="3" maxlength="120"
               placeholder="e.g. Black wallet, Blue umbrella" value="<c:out value='${param.title}'/>">

        <label for="category">Category</label>
        <select id="category" name="category" required>
            <option value="">-- choose --</option>
            <c:forEach var="cat" items="${categories}">
                <option value="<c:out value='${cat}'/>" ${param.category == cat ? 'selected' : ''}><c:out value="${cat}"/></option>
            </c:forEach>
        </select>

        <label for="location">Place (where it was lost / found)</label>
        <input type="text" id="location" name="location" list="place-list" required maxlength="120"
               value="<c:out value='${param.location}'/>">
        <datalist id="place-list">
            <c:forEach var="loc" items="${locations}"><option value="<c:out value='${loc}'/>"></option></c:forEach>
        </datalist>

        <label for="eventDate">Date</label>
        <input type="date" id="eventDate" name="eventDate" required max="${today}"
               value="${empty param.eventDate ? today : param.eventDate}">

        <label for="description">Description (colour, brand, marks - but keep one detail secret to verify the owner)</label>
        <textarea id="description" name="description" rows="4" maxlength="1000"><c:out value="${param.description}"/></textarea>

        <button type="submit" class="btn btn-primary btn-block">Post</button>
    </form>
</div>

<jsp:include page="fragments/footer.jsp"/>
