# Viva Guide — Campus Lost & Found Portal

Everything your team needs to **explain** this project. Each concept says *what it is*, *where it is in our code*, and *what to say*.

---

## 0. One-minute project summary (say this first)

> "Our project is a Campus Lost & Found Portal. Students can register, post items they lost or found, search and filter reports, and send a claim message to the person who posted it. The owner approves or rejects the claim, and only after approval the contact details are shared. We built it with Servlets, JSP and JDBC using the MVC pattern: servlets are controllers, JSPs are views, and DAO classes with JDBC are the model layer. We also used a Filter for login protection, Listeners for start-up and session counting, and sessions for login."

---

## 1. Architecture: MVC + DAO

**MVC (Model–View–Controller)** separates a web application into three parts:

| Part | Job | In our project |
|---|---|---|
| **Model** | Data + database access | `model/` beans (`User`, `Item`, `Claim`) and `dao/` classes |
| **View** | What the user sees | JSP files in `WEB-INF/views/` |
| **Controller** | Receives the request, decides what to do | Servlets in `servlet/` |

**Why it is good:** HTML is not mixed with SQL. A designer can change the JSP without touching Java; a database change affects only the DAO.

**DAO (Data Access Object) pattern:** one class per table (`UserDAO`, `ItemDAO`, `ClaimDAO`) that contains *all* the SQL for that table. Servlets call methods like `itemDao.search(...)` and never write SQL themselves.

**JSPs are inside `WEB-INF`** — files there cannot be opened directly from the browser; they can only be reached through a servlet (`forward`). This forces every request to go through a controller.

---

## 2. Servlets

A **servlet** is a Java class that runs on the server (inside Tomcat, the *servlet container*) and handles HTTP requests.

- Our servlets extend `HttpServlet` and override `doGet()` (page requests) and/or `doPost()` (form submissions).
- Mapped to a URL with an annotation: `@WebServlet("/items")`.
- **Lifecycle:** container loads class → `init()` once → `service()` → `doGet/doPost()` for every request → `destroy()` at shutdown. *One servlet object handles many requests (multi-threaded)* — so servlets must not keep per-user data in instance fields. (That is why our DAOs are stateless.)

**GET vs POST:** GET shows pages / searches (data in URL, can be bookmarked). POST changes data (login, post item, claim) — data is in the request body.

**Typical servlet in our project (`ItemListServlet`):**
1. Read parameters: `request.getParameter("q")`
2. Ask the DAO: `itemDao.search(...)`
3. Store result: `request.setAttribute("items", list)`
4. Forward to the view: `request.getRequestDispatcher("/WEB-INF/views/items.jsp").forward(req, resp)`

**forward vs redirect (very common question):**
- `forward` — happens **inside the server**, same request, the URL in the browser does not change. We use it to show a JSP.
- `sendRedirect` — tells the **browser** to make a *new* request to another URL. We use it **after a successful POST** (Post-Redirect-Get pattern) so pressing browser refresh does not submit the form twice.

---

## 3. JSP, EL and JSTL

**JSP (JavaServer Pages)** = HTML with dynamic parts. Tomcat converts each JSP into a servlet automatically (first time it is requested).

We **do not write Java code (scriptlets) inside JSP**. We use:
- **EL (Expression Language):** `${item.title}` — reads attributes from page / request / session / application scope and calls getters (`getTitle()`).
- **JSTL (JSP Standard Tag Library):**
  - `<c:forEach>` loop, `<c:if>` / `<c:choose>` conditions, `<c:out>` print safely, `<c:remove>` remove attribute
  - `<fmt:formatDate>` format dates

**`<c:out>` and XSS:** `<c:out value="${item.title}"/>` converts `<` to `&lt;` etc. If a user posts the title `<script>alert(1)</script>` it is shown as text instead of running. This is how we prevent **Cross-Site Scripting (XSS)**.

**Include mechanisms (we use both):**
- `<jsp:include page="fragments/header.jsp"/>` — **dynamic include** at request time (header and footer).
- `<%@ include file="item-card.jspf" %>` — **static include**, merged at translation time (the item card used inside loops).

**The 4 scopes:** `page` → `request` → `session` → `application`
| Scope | Example in our project |
|---|---|
| request | `items`, `item`, `errors` (set by servlet, used by JSP, then gone) |
| session | `user` (logged-in user), `flash` message |
| application | `categories`, `ctx`, `activeSessions` (set by listener, shared by everyone) |

---

## 4. JDBC

**JDBC** = the Java API to talk to a relational database. Our steps (see `DBUtil` + any DAO):
1. Load driver — `Class.forName(...)` (`DBUtil` static block)
2. Get connection — `DriverManager.getConnection(url, user, password)`
3. Create statement — **`PreparedStatement`**
4. Execute — `executeQuery()` for SELECT, `executeUpdate()` for INSERT/UPDATE/DELETE
5. Read results — `ResultSet` (`rs.next()`, `rs.getString("title")`)
6. Close everything — **try-with-resources** closes automatically

**Why `PreparedStatement` and not `Statement`?**
- SQL uses `?` placeholders; values are sent separately → **prevents SQL injection** (we tested: searching `' OR '1'='1` just finds nothing).
- Faster when repeated (pre-compiled).

**Transactions** (`ItemDAO.delete()` and `ClaimDAO.decide()`):
```java
con.setAutoCommit(false);   // begin
... several UPDATE/DELETE statements ...
con.commit();               // all succeed → save
// on SQLException:  con.rollback();   → undo everything
```
*Example:* approving a claim does three updates (claim → APPROVED, other pending claims → REJECTED, item → RESOLVED). They must all happen or none — otherwise the data would be inconsistent. This is the **A in ACID (Atomicity)**.

**Other SQL concepts we used:** `JOIN` (item + owner name), correlated sub-query `COUNT(*)` for pending claims, `FOREIGN KEY`, `UNIQUE` (e-mail), `AUTO_INCREMENT`, `LIKE` for search, `ORDER BY ... DESC`, `LIMIT`.

**Honest limitation:** we use `DriverManager` (a new connection per call). Real applications use a **connection pool** (`javax.sql.DataSource`) for speed.

**Why H2?** It is an embedded Java database — no installation. We set `MODE=MySQL`, so the same SQL works on MySQL; switching is only a change in `db.properties`.

---

## 5. Filters

A **Filter** intercepts a request *before* it reaches the servlet (and the response on the way back). Configured with `@WebFilter`.

| Filter | Mapped to | Job |
|---|---|---|
| `EncodingFilter` | `/*` (everything) | sets UTF-8 so all characters display correctly |
| `AuthFilter` | `/items`, `/item`, `/post`, `/my-items`, `/claim`, `/manage`, `/decide` (everything except login, sign-up and the CSS) | checks the session for a logged-in `user`; if absent → redirect to `/login`; if present → `chain.doFilter(...)` |

Key line: `chain.doFilter(request, response)` = "continue to the next filter / the servlet". Not calling it = blocking the request.

**Authentication vs Authorization (often asked):**
- **Authentication** = *who are you?* → login + `AuthFilter`.
- **Authorization** = *are you allowed to do this?* → e.g. `ManageItemServlet` checks `item.getUserId() == user.getId()` so you cannot delete someone else's item even if you are logged in (returns HTTP 403).

---

## 6. Listeners

A **Listener** reacts to events in the web application's life.

| Listener | Interface | Event | What we do |
|---|---|---|---|
| `AppStartupListener` | `ServletContextListener` | application starts | create tables from `schema.sql`, insert demo data, put shared values in application scope |
| `SessionCounterListener` | `HttpSessionListener` | session created / destroyed | increase / decrease a counter shown in the footer |

---

## 7. Sessions and cookies

HTTP is **stateless** — the server forgets you after each request. A **session** gives each browser a memory:
- On first visit Tomcat creates an `HttpSession` and sends the browser a cookie `JSESSIONID`.
- The browser sends that cookie back on each request, so the server finds the same session.
- At login we do `session.setAttribute("user", user)`; at logout `session.invalidate()`.
- Timeout: 30 minutes of inactivity (`web.xml` and `setMaxInactiveInterval`).

**Security details we added (good bonus points):**
- `request.changeSessionId()` after login → prevents *session fixation*.
- Session cookie is `HttpOnly` (JavaScript cannot read it) — set in `web.xml`.
- We never store the password hash in the session.
- Logout is POST-only.
- After login we redirect only to paths starting with `/` (prevents *open redirect*).

**Flash messages:** a message like "Item deleted" is saved in the session by a servlet, then the next page shows it once and removes it (`<c:remove>` in `header.jsp`). Needed because we redirect after POST (request attributes would be lost).

---

## 8. Page protection and caching

- `EncodingFilter` runs on every request: it forces UTF-8 and sends `Cache-Control: no-store`, so after logout the Back button cannot show a protected page from the browser cache.
- `AuthFilter` only lets logged-in users through to the protected URLs (see section 4).

---

## 9. Passwords

We never store passwords. `PasswordUtil` stores a random **salt** and `SHA-256(salt + password)`.
- *Hash* = one-way fingerprint; the password cannot be recovered.
- *Salt* = random value so two users with the same password have different hashes and pre-computed tables don't work.
- Login: hash the typed password with the user's salt and compare.
- Limitation to mention: production systems use slower algorithms like bcrypt / Argon2.

---

## 10. web.xml and annotations

- **Annotations** (`@WebServlet`, `@WebFilter`, `@WebListener`, `@MultipartConfig`) configure components right in the Java class (Servlet 3.0+).
- **`web.xml`** (deployment descriptor) still holds what annotations can't: session timeout, cookie settings, custom error pages (404/403/500 → `error.jsp`).

---

## 11. Server-side validation

Browser checks (`required`, `minlength`, `type="email"`) are only for convenience — they can be bypassed. **All real checks are repeated in the servlet** (lengths, e-mail format, allowed category, date not in the future, duplicate e-mail). On error the form is shown again with the messages and the typed values kept.

---

## 12. Likely questions from the teacher (with answers)

1. **Why Tomcat 10 / `jakarta.*` instead of `javax.*`?**
   Java EE moved to the Eclipse Foundation and was renamed Jakarta EE. From Servlet 5 / Tomcat 10 the packages are `jakarta.servlet.*`. Concepts are identical to `javax.servlet`.

2. **Difference between `doGet` and `doPost`?** GET: data in URL, idempotent, for fetching. POST: data in body, for changing data, not cached/bookmarked.

3. **Difference between `forward()` and `sendRedirect()`?** See section 2.

4. **What is the servlet lifecycle?** `init → service(doGet/doPost) → destroy`; one instance, many threads.

5. **How does your application stay logged in?** Session + `JSESSIONID` cookie; `user` object in the session; `AuthFilter` checks it.

6. **Why PreparedStatement?** SQL injection protection + performance.

7. **What is a transaction? Where did you use it?** A group of statements that succeed or fail together (commit/rollback). In `ClaimDAO.decide()` and `ItemDAO.delete()`.

8. **What is the difference between `include` directive and `jsp:include`?** Directive = static, at translation time. Action = dynamic, at request time.

9. **How do you prevent XSS / SQL injection?** `<c:out>` escaping; `PreparedStatement`.

10. **Why are JSPs under `WEB-INF`?** So they can't be requested directly; all access goes through controllers.

11. **What is the difference between `request`, `session` and `application` scope?** One request / one user's visit / the whole application for all users.

12. **How does your search work?** `ItemDAO.search()` builds the `WHERE` clause only for the filters the user filled in (type, category, status, keyword with `LIKE`), always using `?` placeholders.

13. **What would you improve?** Connection pool, notifications, admin panel, bcrypt, CSRF tokens, REST API, auto-matching of lost and found items.

14. **Why MVC? What if the database changes?** Only the DAO classes (and `db.properties`) change; servlets and JSPs stay the same.

15. **What is a JavaBean?** A class with a no-argument constructor, private fields and public getters/setters — so EL can read `${item.title}` (calls `getTitle()`). Our `User`, `Item`, `Claim`.

16. **Why is `User` `Serializable`?** Objects stored in a session may be saved to disk or copied between servers; they must be serializable.

17. **What does `@WebServlet(urlPatterns = {"", "/home"})` mean in `HomeServlet`?** The empty pattern matches the application root, so `/` and `/home` both show the home page.

---

## 13. Walk-through of one complete flow: "Owner approves a claim"

1. Owner opens `/my-items` → `MyItemsServlet` → DAOs → `my-items.jsp` shows "1 waiting".
2. Clicks the item → `ItemDetailServlet` finds that the viewer *is the owner* → loads claims → `item-detail.jsp` shows Approve / Reject buttons.
3. Clicks **Approve** → browser POSTs to `/decide` → `AuthFilter` lets it through (logged in).
4. `ClaimDecisionServlet` checks authorization (owner of the item?), then calls `claimDao.decide(...)` — one transaction.
5. Servlet stores a flash message and **redirects** to `/item?id=...` (Post-Redirect-Get).
6. The claimer opens the same page: `ItemDetailServlet` sees an APPROVED claim and sets `showOwnerContact = true`, so the JSP prints the owner's e-mail and phone.
