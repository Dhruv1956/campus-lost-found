# Campus Lost & Found Portal

**Author:** Dhruv Upadhyaya ([@Dhruv1956](https://github.com/Dhruv1956)) · Advanced Java & J2EE · 2026

## Project report

📄 [Read the project report (PDF)](docs/Campus_Lost_Found_Report.pdf) — covers the introduction, the pseudocode of the implementation, the results with screenshots and the conclusion.

**Advanced Java & J2EE mini project** — a web portal where students post items they lost or found on campus, search for them, and contact each other safely.

Built with: **Servlets · JSP · JSTL/EL · JDBC · Filters · Listeners · HttpSession · MVC · DAO pattern** (Jakarta EE 10 / Tomcat 10.1, Java 17+).
No Spring, no Hibernate, no heavy frameworks — everything uses the core J2EE concepts from the course.

---

> **On Windows? Open `START_HERE_WINDOWS.md` first - step-by-step setup.**

## 1. What the portal does

| Feature | Where it is in the code |
|---|---|
| Sign up / log in / log out (password stored as salted SHA-256 hash) | `RegisterServlet`, `LoginServlet`, `LogoutServlet`, `PasswordUtil` |
| Post a **LOST** or **FOUND** item with category, place, date | `PostItemServlet`, `post-item.jsp` |
| Browse + **search & filter** (keyword, type, category, open/resolved) | `ItemListServlet`, `ItemDAO.search()`, `items.jsp` |
| Item detail page — different view for owner / other user / visitor | `ItemDetailServlet`, `item-detail.jsp` |
| **Claim / contact** workflow: send a message → owner approves or rejects → contact details are revealed only after approval | `ClaimServlet`, `ClaimDecisionServlet`, `ClaimDAO.decide()` |
| Owner can mark resolved or delete (only their own items) | `ManageItemServlet` |
| "My items" dashboard (items I posted + messages I sent) | `MyItemsServlet`, `my-items.jsp` |
| Pages that need login are protected | `AuthFilter` |
| Database tables are created and demo data inserted automatically at start-up | `AppStartupListener`, `schema.sql` |
| Live "active sessions" counter in the footer | `SessionCounterListener` |

---

## 2. How to run it

### What you need installed
1. **JDK 17 or newer** (17 or 21 recommended) — check with `java -version`
2. **Apache Maven** — check with `mvn -version`

On a Mac with Homebrew:
```bash
brew install openjdk@21 maven
```
On Windows: install a JDK (e.g. Temurin 21) and Maven, and make sure `java` and `mvn` work in a terminal.

### Run (easiest — no Tomcat install needed)
```bash
cd campus-lost-found
mvn compile exec:java
```
(or double-click / run `./run.sh` on Mac/Linux, `run.bat` on Windows)

The first run downloads libraries (needs internet, takes a minute or two). When you see the green banner, open:

**http://localhost:8080/**

Stop the server with `Ctrl + C`.

### Demo accounts (created automatically)
| E-mail | Password |
|---|---|
| `demo@campus.edu` | `demo123` |
| `priya@campus.edu` | `demo123` |

### Alternative: deploy as a WAR on a normal Tomcat 10.1
```bash
mvn package
```
Copy `target/campus-lost-found.war` into the `webapps` folder of **Tomcat 10.1.x** (not Tomcat 9 — it uses the `jakarta.*` packages) and start Tomcat.
Open **http://localhost:8080/campus-lost-found/**

---

## 3. Project structure (MVC)

```
campus-lost-found/
├── pom.xml                         Maven build file (dependencies)
├── run.sh / run.bat                one-command start scripts
└── src/main/
    ├── java/com/campus/lostfound/
    │   ├── model/                  M  - plain Java beans: User, Item, Claim
    │   ├── dao/                    M  - all SQL (JDBC): UserDAO, ItemDAO, ClaimDAO
    │   ├── servlet/                C  - controllers: one servlet per page / action
    │   ├── filter/                    - EncodingFilter, AuthFilter
    │   ├── listener/                  - AppStartupListener, SessionCounterListener
    │   ├── util/                      - DBUtil (connections), PasswordUtil, WebUtil
    │   └── RunServer.java             - starts embedded Tomcat (only for "mvn exec:java")
    ├── resources/
    │   ├── db.properties           database URL / user / password
    │   └── schema.sql              CREATE TABLE statements
    └── webapp/
        ├── css/style.css
        └── WEB-INF/
            ├── web.xml             session timeout, error pages
            └── views/              V  - JSP pages (hidden from direct URL access)
                ├── home.jsp, items.jsp, item-detail.jsp, post-item.jsp,
                │   my-items.jsp, login.jsp, register.jsp, error.jsp
                └── fragments/      header.jsp, footer.jsp, item-card.jspf
```

### Request flow (same for every page)
```
Browser ──► Filter(s) ──► Servlet (controller) ──► DAO ──► Database
                              │                      │
                              │◄──── model objects ──┘
                              ▼
                     request.setAttribute(...)
                              ▼
                        JSP (view) + JSTL/EL  ──► HTML back to browser
```

### Database (3 tables)
```
users(id, full_name, email UNIQUE, phone, salt, password_hash, created_at)
items(id, user_id → users, type LOST/FOUND, title, description, category,
      location, event_date, status OPEN/RESOLVED, created_at)
claims(id, item_id → items, claimer_id → users, message,
       status PENDING/APPROVED/REJECTED, created_at)
```
The default database is **H2 (embedded)** — it needs no installation; data is saved in the folder `~/campus-lostfound-data/`.
To reset everything: stop the server and delete that folder.

### Using MySQL instead of H2 (if your teacher expects MySQL)
1. In `pom.xml` add the dependency `com.mysql:mysql-connector-j:8.4.0`.
2. In MySQL run: `CREATE DATABASE campus_lostfound;`
3. In `src/main/resources/db.properties` comment out the H2 lines and un-comment the MySQL lines (put your MySQL password).
4. Run again — the tables and demo data are created automatically. No Java code changes are needed (that is the benefit of the DAO + `db.properties` design).

---

## 4. Suggested 10-minute demo (for the evaluation)

| Time | Who | What to show |
|---|---|---|
| 0:00 – 1:00 | Member 1 | Problem statement: things get lost on campus, notices on WhatsApp/boards get buried. Show the **home page** and stats. |
| 1:00 – 2:30 | Member 2 | **Sign up** a new user (show validation errors), then **log in**. Show that `/post` redirects to login when logged out (**AuthFilter**). |
| 2:30 – 4:30 | Member 3 | **Post a found item**. Browse, **search** "wallet", filter by type/category (**JDBC + PreparedStatement**). |
| 4:30 – 6:30 | Member 4 | Log in as a second user, open the item, **send a claim message**. Log back in as owner → **approve** → contact details appear for the claimer. Explain the **transaction** (approve + reject others + resolve). |
| 6:30 – 9:00 | Member 1 + 2 | Show the **code**: MVC folder structure, one servlet → DAO → JSP chain, `web.xml`, filter, listener. |
| 9:00 – 10:00 | All | Limitations and future scope (see below), questions. |

Read `VIVA_GUIDE.md` — it explains every concept in plain words and has likely teacher questions with answers.

---

## 5. Limitations / future scope (good to say out loud)
- Uses `DriverManager`; a real deployment would use a **connection pool** (`DataSource`).
- No e-mail/SMS notification; no admin panel; no CSRF tokens.
- Password hashing is SHA-256 + salt (simple to explain); production systems use bcrypt / Argon2.
- Could be extended with a REST API + mobile app, automatic matching of lost/found items by keywords, and moderation.

---

## 6. Troubleshooting

| Problem | Fix |
|---|---|
| `mvn: command not found` | Install Maven (see section 2) and open a new terminal. |
| `release version 17 not supported` | Your JDK is older than 17. Install JDK 17 or 21. |
| `Address already in use` / port 8080 busy | Run on another port: `PORT=9090 mvn compile exec:java` (Windows: `set PORT=9090` first). |
| JSP error mentioning "Unsupported class file major version" | Your JDK is too new for the bundled JSP compiler. Use JDK 21: `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` (Mac). |
| Browser shows an old page / odd data | Delete the `~/campus-lostfound-data` folder and restart. |
| Anything else | Copy the red error text from the terminal and ask Claude Code / Claude — it is usually a one-line fix. |
