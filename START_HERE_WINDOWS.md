# START HERE (Windows) - run the project in 5 steps

You need internet for the first run only (it downloads libraries once).

## 1. Install Java (JDK 21)
Open **PowerShell** and run:
```
winget install EclipseAdoptium.Temurin.21.JDK
```
(or download "Temurin 21 JDK" for Windows from https://adoptium.net and run the installer; tick "Set JAVA_HOME" and "Add to PATH" if it asks.)
**Close and reopen PowerShell**, then check:
```
java -version
```
It must say version 21.

## 2. Install Maven
1. Download the **Binary zip archive** from https://maven.apache.org/download.cgi (apache-maven-3.x.x-bin.zip).
2. Unzip it to `C:\maven` (so that `C:\maven\bin\mvn.cmd` exists).
3. Press Windows key -> type "environment variables" -> **Edit the system environment variables** -> **Environment Variables**.
4. Under "User variables" select **Path** -> Edit -> New -> `C:\maven\bin` -> OK.
5. Under "User variables" -> New: name `JAVA_HOME`, value = your JDK folder (e.g. `C:\Program Files\Eclipse Adoptium\jdk-21.x.x-hotspot`) - only if `mvn -version` complains about JAVA_HOME.
6. **Close and reopen PowerShell**, then check:
```
mvn -version
```
It must print a Maven version and Java 21.

## 3. Unzip the project
Unzip `campus-lost-found.zip` anywhere (e.g. your Desktop).

## 4. Run it
In PowerShell:
```
cd $HOME\Desktop\campus-lost-found
mvn compile exec:java
```
(Or just double-click `run.bat`.) The first run takes 1-3 minutes. Wait until you see **"Database ready"** and the banner with the address.

## 5. Open the website
Go to **http://localhost:8080/** in Chrome. You land on the login page.

Demo logins (password for both: `demo123`):
- `demo@campus.edu`
- `priya@campus.edu`

Stop the server with `Ctrl + C`.

---

## If something goes wrong
| Problem | Fix |
|---|---|
| `mvn is not recognized` | Step 2 PATH is wrong, or you did not reopen PowerShell. |
| `JAVA_HOME ... not defined correctly` | Set `JAVA_HOME` (step 2, point 5) to the JDK folder, reopen PowerShell. |
| `Address already in use` / port 8080 busy | Close the other program, or run `$env:PORT=9090; mvn compile exec:java` and open http://localhost:9090/ |
| Weird old data on the site | Stop the server, delete the folder `C:\Users\<your name>\campus-lostfound-data`, start again. |
| Anything else | Copy the red error text and ask Claude / ChatGPT; it is usually a one-line fix. |

## Before you present
- Read `VIVA_GUIDE.md` (every concept explained simply + likely questions).
- Read the "Suggested 10-minute demo" in `README.md`.
- Do one full practice run so the first start (library download) is not done in front of the examiner. Start the server BEFORE your slot.
