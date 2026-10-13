# Oorum Blood – Blood Bank Management System

> **Academic Project** | OOP (Java 17) + DBMS (MySQL) | No frameworks, no build tools.

---

## 📁 Project Structure

```
BloodBankManagementSystem/
├── database/
│   └── schema.sql              ← Manual SQL setup (auto-applied on startup too)
├── design/                     ← Stitch UI export (reference)
├── lib/                        ← Place mysql-connector-j-*.jar here
├── out/                        ← Compiled .class files (auto-created by run.bat)
├── src/
│   ├── Main.java               ← Entry point
│   ├── db/
│   │   └── DatabaseConnection.java   ← JDBC config (change password here)
│   ├── model/
│   │   ├── Person.java         ← Abstract base class
│   │   ├── Donor.java
│   │   ├── Patient.java
│   │   ├── Hospital.java
│   │   ├── BloodStock.java
│   │   ├── Donation.java
│   │   └── BloodRequest.java
│   ├── dao/
│   │   ├── CrudOperations.java ← Generic CRUD interface
│   │   ├── DonorDao.java
│   │   ├── PatientDao.java
│   │   ├── HospitalDao.java
│   │   ├── BloodStockDao.java
│   │   ├── DonationDao.java
│   │   └── BloodRequestDao.java
│   ├── server/
│   │   ├── BaseHandler.java
│   │   ├── StaticFileHandler.java
│   │   ├── DashboardHandler.java
│   │   ├── DonorHandler.java
│   │   ├── PatientHandler.java
│   │   ├── HospitalHandler.java
│   │   ├── BloodStockHandler.java
│   │   ├── DonationHandler.java
│   │   └── BloodRequestHandler.java
│   └── util/
│       └── JsonUtil.java       ← Hand-rolled JSON parser/serializer
├── web/
│   ├── index.html              ← Single-page application shell
│   ├── css/style.css
│   └── js/app.js
├── run.bat                     ← Windows: compile + run
├── run.sh                      ← Linux/macOS: compile + run
└── README.md
```

---

## ⚙️ Prerequisites

| Requirement | Version |
|---|---|
| JDK | 17+ |
| MySQL Server | 8.0+ |
| MySQL Connector/J | 8.x or 9.x |

---

## 🚀 How to Run

### Step 1 – Place the MySQL Connector/J jar in `lib/`

Download from https://dev.mysql.com/downloads/connector/j/ and drop the `.jar` file into the `lib/` folder.

### Step 2 – Set your MySQL password

Open `src/db/DatabaseConnection.java` and change the password line:

```java
private static final String PASSWORD = "root"; // <-- CHANGE PASSWORD HERE
```

### Step 3 – Compile and start

**Windows:**
```bat
run.bat
```

**Linux / macOS:**
```bash
chmod +x run.sh
./run.sh
```

### Step 4 – Open in browser

```
http://localhost:8080
```

The application will automatically create the `bloodbank` database and all tables on first run.

---

## 🩸 Features

| Screen | Features |
|---|---|
| **Dashboard** | KPI counters (donors, patients, units, pending requests), bar chart & donut for stock, recent requests and donations |
| **Donor Management** | Register / edit / delete donors; live search by name or blood group |
| **Patient Management** | Add / edit / delete patients |
| **Hospital Management** | Register / edit / delete hospitals |
| **Blood Inventory** | 8 blood-group cards with progress bars; reserve summary table |
| **Blood Donations** | Record donations (increases stock); full intake log |
| **Blood Requests** | Create, fulfill (checks stock), reject, delete; filter by status; stats KPIs |

---

## 🎓 OOP Concepts (for Viva)

| Concept | Where |
|---|---|
| **Abstract Class** | `Person` – abstract `displayDetails()` |
| **Inheritance** | `Donor`, `Patient`, `Hospital` extend `Person` |
| **Polymorphism** | Each subclass overrides `displayDetails()` |
| **Encapsulation** | All model fields are `private` with getters/setters |
| **Interface** | `CrudOperations<T>` implemented by `DonorDao`, `PatientDao`, `HospitalDao` |
| **Association** | `BloodRequest` links `Patient` (patientId) and `Hospital` (hospitalId) |
| **Abstraction** | `BaseHandler` provides abstract HTTP helpers; DAO interface hides SQL details |
| **Packages** | `model`, `dao`, `db`, `server`, `util` |

---

## 🗄️ DBMS Concepts (for Viva)

| Concept | Where |
|---|---|
| **Schema** | 6 tables with PK, FK, UNIQUE, CHECK constraints |
| **Foreign Keys** | `donation.donor_id → donor.id` (CASCADE), `blood_request → patient + hospital` (CASCADE) |
| **PreparedStatement** | Used in every DAO query – prevents SQL injection |
| **Transactions** | `DonationDao.recordDonation()` and `BloodRequestDao.fulfill()` use `conn.setAutoCommit(false)` |
| **JOIN queries** | Donation list JOINs donor name; request list JOINs patient + hospital names |
| **Normalization** | 3NF – no repeating groups, no partial/transitive dependencies |
| **Auto-initialization** | `CREATE DATABASE IF NOT EXISTS` + `CREATE TABLE IF NOT EXISTS` on startup |

---

## 🔌 REST API Reference

| Endpoint | Method | Description |
|---|---|---|
| `/api/dashboard` | GET | KPIs, stock, recent items |
| `/api/donors` | GET | List all donors |
| `/api/donors?search=...` | GET | Search donors |
| `/api/donors` | POST | Create donor |
| `/api/donors/{id}` | PUT | Update donor |
| `/api/donors/{id}` | DELETE | Delete donor |
| `/api/patients` | GET/POST | List / create |
| `/api/patients/{id}` | PUT/DELETE | Update / delete |
| `/api/hospitals` | GET/POST | List / create |
| `/api/hospitals/{id}` | PUT/DELETE | Update / delete |
| `/api/stock` | GET | All blood group stock |
| `/api/donations` | GET | All donations |
| `/api/donations` | POST | Record donation (updates stock) |
| `/api/requests` | GET | All blood requests |
| `/api/requests` | POST | Create request |
| `/api/requests/{id}/fulfill` | PUT | Fulfill (deducts stock) |
| `/api/requests/{id}/reject` | PUT | Reject request |
| `/api/requests/{id}` | DELETE | Delete request |

---

## ⚠️ Troubleshooting

| Problem | Fix |
|---|---|
| `Communications link failure` | MySQL is not running. Start the service. |
| `Access denied for user 'root'` | Wrong password in `DatabaseConnection.java` |
| `No jar found in lib/` | Download and place `mysql-connector-j-*.jar` in `lib/` |
| Port 8080 in use | Change port in `Main.java` (`int PORT = 8080`) |
| Compilation error on Java < 17 | Use JDK 17+ (text blocks, pattern matching) |
