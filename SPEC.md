# DataLNI — Technical Specification

A desktop application to manage (CRUD) data in a **DB2** database, built as a
**single JVM** combining a **Spring Boot 4.0.6** backend (service + persistence
layers) with a **JavaFX** desktop frontend. Authentication is delegated to
**LDAP / Active Directory**.

---

## 1. Overview

| Item | Decision |
|------|----------|
| Project name | DataLNI |
| Architecture | Single application — JavaFX UI + Spring beans in one JVM, **direct bean calls** (no REST layer) |
| Backend | Spring Boot 4.0.6, Spring Data JPA / Hibernate |
| Frontend | JavaFX (FXML-based views, controllers wired to Spring beans) |
| Database | IBM DB2, schema **`PLD`** |
| Auth | LDAP / Active Directory (login on startup, role-based UI gating) |
| Java runtime | Java 21 LTS (Spring Boot 4.0 baseline is Java 17; 21 recommended) |

### Goals
- Provide CRUD screens for the four `PLD` entities below.
- Reuse a single Spring `ApplicationContext` to manage persistence, transactions, and security inside the JavaFX app.
- Authenticate users against corporate AD and restrict edit/delete actions by AD group.

### Non-goals
- No web/HTTP API (explicitly single-JVM, direct calls).
- No multi-tenant / multi-DB support.
- No offline mode — the app requires a live DB2 + AD connection.

---

## 2. Technology Stack & Versions

| Component | Version / Artifact | Notes |
|-----------|--------------------|-------|
| JDK | 21 (LTS) | Spring Boot 4.0 requires Java 17+ |
| Spring Boot | 4.0.6 | Built on Spring Framework 7, Jakarta EE 11 |
| Spring Data JPA | (managed by Boot 4.0.6) | Hibernate ORM as provider |
| Spring Security LDAP | (managed by Boot) | `spring-security-ldap` for AD bind/auth |
| JavaFX | 21 (LTS) | `org.openjfx:javafx-controls`, `javafx-fxml` |
| UI theme | AtlantaFX (`io.github.mkpaz:atlantafx-base`) | Modern CSS theme pack; Primer Light default + Primer Dark toggle |
| DB2 JDBC driver | `com.ibm.db2:jcc:11.5.9.0` (or site-approved) | IBM Data Server Driver (JCC) |
| Build tool | Maven (recommended) or Gradle | JavaFX + Spring Boot plugin coordination needed |
| Logging | SLF4J + Logback (Boot default) | |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) | |
| Testing | JUnit 5, Testcontainers (DB2), TestFX (optional UI) | |

> **DB2 dialect:** configure Hibernate with the DB2 dialect (`org.hibernate.dialect.DB2Dialect` / LUW or z/OS variant matching the target). Confirm whether the DB2 target is LUW or z/OS — it affects dialect and pagination.

---

## 3. Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                     Single JVM (DataLNI.app)                   │
│                                                                │
│  ┌────────────────┐   FXML/Controllers   ┌──────────────────┐ │
│  │  JavaFX UI      │  ◄────────────────►  │  Spring context  │ │
│  │  (Stages,       │   controllerFactory  │  (beans)         │ │
│  │   Scenes,       │   = Spring resolves   │                  │ │
│  │   Controllers)  │     controllers      │  ┌────────────┐  │ │
│  └────────────────┘                       │  │ @Service   │  │ │
│         ▲ direct call                     │  │  layer     │  │ │
│         │                                 │  └─────┬──────┘  │ │
│         │                                 │        ▼         │ │
│         │                                 │  ┌────────────┐  │ │
│         └─────────────────────────────────┤  │ @Repository│  │ │
│                                           │  │ (Spring    │  │ │
│                                           │  │  Data JPA) │  │ │
│  ┌────────────────┐                       │  └─────┬──────┘  │ │
│  │ Security ctx   │  ◄────────────────────┤        │         │ │
│  │ (current user, │   AuthenticationMgr   │        │         │ │
│  │  roles)        │                       └────────┼─────────┘ │
│  └────────────────┘                                │           │
└────────────────────────────────────────────────────┼───────────┘
                                                      ▼
                                          ┌─────────────────────┐
                  LDAP/AD  ◄──── bind ────│  DB2 (schema PLD)    │
                                          └─────────────────────┘
```

### Layering
1. **UI layer** — FXML views + JavaFX controllers. Controllers depend on service beans, never on repositories directly.
2. **Service layer** (`@Service`, `@Transactional`) — business rules, validation orchestration, transaction boundaries, security checks (`@PreAuthorize`).
3. **Persistence layer** (`@Repository`, Spring Data JPA) — entity mapping, queries.
4. **Security** — LDAP `AuthenticationManager` + Spring Security method security.

---

## 4. Spring Boot + JavaFX Integration

Because there is **one JVM with direct calls**, the two frameworks must share a lifecycle. Recommended pattern:

1. `main()` launches the JavaFX `Application` (`Application.launch(...)`).
2. In `Application.init()` (runs off the FX thread), bootstrap Spring **headless** (no web server):
   ```java
   springContext = new SpringApplicationBuilder(DataLniApplication.class)
       .web(WebApplicationType.NONE)   // no embedded server
       .run(args);
   ```
3. In `start(Stage)`, set the FXML `controllerFactory` to resolve controllers from Spring so controllers can be injected with service beans:
   ```java
   FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
   loader.setControllerFactory(springContext::getBean);
   ```
4. In `stop()`, close the Spring context.

**Threading rule:** all DB/service calls run **off the JavaFX Application Thread**. Use a `javafx.concurrent.Task` (or `Service`) for each repository/service invocation, and update the UI only in `succeeded()`/`Platform.runLater(...)`. A shared `@Bean` `TaskExecutor` (e.g. a bounded thread pool) backs long-running calls.

**Disambiguation:** Spring Boot's `@SpringBootApplication` class is *not* the JavaFX `Application` subclass — keep them separate (`DataLniApplication` for Spring config, `DataLniFxApp extends javafx.application.Application` for the UI bootstrap).

---

## 5. Data Model

All tables are in schema **`PLD`**. Java types in parentheses.

### 5.1 Graph — `PLD.LNI_GRAFICO`
| Java field | Column | DB type | Notes |
|-----------|--------|---------|-------|
| `id` (Integer) | `CD_GRAFICO` | INTEGER | **PK** |
| `description` (String) | `DE_DESCRICAO` | VARCHAR(150) | |
| `title` (String) | `DE_TITULO` | VARCHAR(150) | |
| `dataNumbers` (List) | — | — | `@OneToMany` → DataNumber via `CD_GRAFICO` |

### 5.2 DataNumber — `PLD.LNI_NUMERO_UEM`
| Java field | Column | DB type | Notes |
|-----------|--------|---------|-------|
| `id` (Integer) | `CD_NUMERO` | INTEGER | **PK** |
| `month` (Integer) | `ME_MES` | INTEGER | |
| `year` (Integer) | `AN_ANO` | INTEGER | |
| `value` (BigDecimal) | `VL_VALOR` | DECIMAL(10,2) | use `BigDecimal`, not `Double` (precision — Q2) |
| `graphId` (Integer) | `CD_GRAFICO` | INTEGER | FK → Graph |
| `clazz` (String) | `DE_CLASSE` | VARCHAR(150) | `class` is a reserved word in Java |

Relationship: **Graph `1` ──< `*` DataNumber** on `CD_GRAFICO`. Maps with a standard single-column `@Id` on `CD_NUMERO`; CRUD via the normal `JpaRepository` (`findById`/`save`/`deleteById`).

### 5.3 Project — `PLD.LNI_PROJETO_PRESTACAO`
| Java field | Column | DB type | Notes |
|-----------|--------|---------|-------|
| `id` (Integer) | `CD_PROJETO` | INTEGER | **PK** |
| `ods` (Integer) | `TP_ODS` | INTEGER | UN SDG code (1–17?) — candidate enum |
| `eprotocol` (String) | `DE_EPROTOCOLO` | VARCHAR(50) | |
| `title` (String) | `DE_TITULO` | VARCHAR(250) | |
| `coordinator` (String) | `DE_COORDENADOR` | VARCHAR(200) | |

### 5.4 Sustainability — `PLD.LNI_SUSTENTABILIDADE`
| Java field | Column | DB type | Notes |
|-----------|--------|---------|-------|
| `id` (Integer) | `CD_SUSTENTABILIDADE` | INTEGER | **PK** |
| `year` (Integer) | `AN_ANO` | SMALLINT | |
| `ods` (Integer) | `TP_ODS` | SMALLINT | UN SDG code |
| `title` (String) | `DE_TITULO` | VARCHAR(250) | |
| `link` (String) | `DE_LINK` | VARCHAR(550) | |
| `author` (String) | `DE_AUTOR` | VARCHAR(200) | |
| `published` (LocalDate) | `DT_PUBLICACAO` | DATE | |

> **PK generation:** the spec assumes `CD_GRAFICO`, `CD_NUMERO`, `CD_PROJETO`, `CD_SUSTENTABILIDADE` are populated by a DB2 sequence or `GENERATED` identity. Confirm per table so the right `@GeneratedValue` strategy (IDENTITY vs SEQUENCE) is chosen. If application-assigned, use a sequence-backed ID service. Also confirm `CD_NUMERO` has an enforced PK/unique constraint (Q1).

---

## 6. Persistence Layer

- One Spring Data repository per entity:
  - `GraphRepository extends JpaRepository<Graph, Integer>`
  - `ProjectRepository extends JpaRepository<Project, Integer>`
  - `SustainabilityRepository extends JpaRepository<Sustainability, Integer>`
  - `DataNumberRepository extends JpaRepository<DataNumber, Integer>` (keyed by `CD_NUMERO`)
- Derived queries for common filters, e.g.:
  - `List<DataNumber> findByGraphId(Integer graphId);`
  - `List<DataNumber> findByGraphIdAndYear(Integer graphId, Integer year);`
  - `List<Project> findByOds(Integer ods);`
  - `List<Sustainability> findByYearOrderByPublishedDesc(Integer year);`
- Pagination: use `Pageable` for large tables; ensure DB2-compatible pagination (Hibernate handles via dialect).
- `hibernate.default_schema=PLD` so unqualified table names resolve to `PLD`.
- `spring.jpa.hibernate.ddl-auto=validate` (never `update`/`create` against the corporate DB).

---

## 7. Service Layer

One `@Service` per aggregate. Each method `@Transactional`, with read methods `@Transactional(readOnly = true)`. Services:
- map entities ↔ UI-facing DTOs/JavaFX models (keep JPA entities out of the FX thread to avoid lazy-loading surprises),
- enforce validation (Bean Validation on DTOs),
- enforce authorization via `@PreAuthorize("hasRole('LNI_EDITOR')")` on mutating methods,
- expose CRUD: `findAll`, `findById`, `search(criteria)`, `create`, `update`, `delete`.

Example surface:
```
GraphService:        list(), get(id), create(dto), update(dto), delete(id),
                     listDataNumbers(graphId)
DataNumberService:   listByGraph(graphId), get(id), create(dto), update(dto), delete(id)
ProjectService:      list(), get(id), create(dto), update(dto), delete(id), searchByOds(ods)
SustainabilityService: list(), get(id), create(dto), update(dto), delete(id)
```

---

## 8. Authentication & Authorization (LDAP / Active Directory)

Because there is no web layer, Spring Security is used **programmatically** (no servlet filter chain):

1. On startup, before showing the main window, present a **login dialog** (username + password).
2. Authenticate via an `AuthenticationManager` backed by AD:
   - Use `ActiveDirectoryLdapAuthenticationProvider(domain, url)` (handles AD `userPrincipalName` bind), **or** a standard `LdapAuthenticationProvider` with bind + group search.
3. On success, store the `Authentication` in Spring's `SecurityContextHolder` (and an app-level `CurrentUser` bean) for the session.
4. **Roles** derived from AD group membership, mapped to app roles, e.g.:
   - `LNI_ADMIN` → full CRUD on all entities
   - `LNI_EDITOR` → create/update/delete
   - `LNI_VIEWER` → read-only (mutating buttons disabled/hidden)
5. **Method security** (`@EnableMethodSecurity`) enforces roles at the service layer; the UI *also* hides/disables controls for UX, but the service is the source of truth.

**Config inputs needed (Q3):** AD domain, LDAP URL(s) (`ldaps://...`), base DN, group search base/filter, and the AD group → app-role mapping. TLS (`ldaps`) strongly recommended; never send credentials over plain LDAP.

> Note: the `SecurityContext` must be propagated to the worker threads that run service calls (use `DelegatingSecurityContextExecutor` wrapping the FX task executor) so `@PreAuthorize` sees the logged-in user.

---

## 9. UI Design (JavaFX)

### Theme
- **AtlantaFX** provides the look-and-feel — a drop-in CSS theme pack covering all standard controls (`TableView`, dialogs, toolbars), so the four modules stay visually consistent with no per-control styling.
- Applied once at startup:
  ```java
  Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
  ```
- **Primer Light** is the default; **Primer Dark** is offered as a user toggle (menu item) — switching themes is a single `setUserAgentStylesheet(...)` call. Project-specific overrides live in `/resources/css/` layered on top of the AtlantaFX base.

### Navigation
- **Login dialog** → **Main window** with a left navigation (or top menu) listing the four modules: *Graphs, Data Numbers, Projects, Sustainability*.
- Status bar shows current user + role; mutating actions hidden for `LNI_VIEWER`.

### Per-module screen (common CRUD pattern)
- **List view**: `TableView` with columns from the entity, a search/filter bar, and toolbar buttons **New / Edit / Delete / Refresh**.
- **Edit form**: modal dialog or detail pane with validated fields; **Save / Cancel**.
- Inline validation messages; confirmation dialog on delete; non-blocking progress indicator while a `Task` runs.

### Module specifics
- **Graphs**: master-detail — selecting a Graph shows its **DataNumbers** in a child table (drill-down via `findByGraphId`). New DataNumber pre-fills `CD_GRAFICO`.
- **Data Numbers**: editable grid (month/year/value/class) scoped to a Graph; delete/update by `CD_NUMERO`. Consider a year filter.
- **Projects**: `ods` shown as label (map SDG code → name if a lookup is provided); free-text search on title/coordinator/eprotocol.
- **Sustainability**: date picker for `published`; `link` rendered as clickable hyperlink; filter by year/ods.

### FXML organization
`/resources/fxml/` — `login.fxml`, `main.fxml`, `graph-list.fxml`, `graph-form.fxml`, `datanumber-*.fxml`, `project-*.fxml`, `sustainability-*.fxml`. One controller per FXML, all Spring-managed (prototype scope for forms).

---

## 10. Configuration

`application.yml` (secrets externalized, **not** committed):
```yaml
spring:
  datasource:
    url: jdbc:db2://<host>:50000/<database>:currentSchema=PLD;
    username: ${DB2_USER}
    password: ${DB2_PASSWORD}
    driver-class-name: com.ibm.db2.jcc.DB2Driver
    hikari:
      maximum-pool-size: 5            # desktop app — small pool
  jpa:
    hibernate.ddl-auto: validate
    properties:
      hibernate.default_schema: PLD
      hibernate.dialect: org.hibernate.dialect.DB2Dialect

app:
  ldap:
    url: ldaps://<ad-host>:636
    domain: corp.example.com
    base-dn: DC=corp,DC=example,DC=com
    group-search-base: OU=Groups
    role-mapping:
      LNI_ADMINS:  LNI_ADMIN
      LNI_EDITORS: LNI_EDITOR
      LNI_USERS:   LNI_VIEWER
```
Credentials/secrets via environment variables or OS credential store, never hard-coded. Spring profiles: `dev` (Testcontainers DB2 / dev AD) and `prod`.

---

## 11. Project Structure (Maven)

```
DataLNI/
├─ pom.xml
├─ SPEC.md
└─ src/main/
   ├─ java/com/lni/datalni/
   │  ├─ DataLniApplication.java        # @SpringBootApplication (config)
   │  ├─ DataLniFxApp.java              # extends javafx.application.Application
   │  ├─ Launcher.java                  # main(); calls Application.launch
   │  ├─ config/                        # Spring config: datasource, security, executors
   │  ├─ domain/                        # JPA entities: Graph, DataNumber, Project, Sustainability
   │  ├─ repository/                    # Spring Data repositories
   │  ├─ service/                       # @Service + DTOs + mappers
   │  ├─ security/                      # LDAP auth provider, CurrentUser, role mapping
   │  └─ ui/                            # JavaFX controllers + FX models
   └─ resources/
      ├─ application.yml
      ├─ fxml/                          # all .fxml views
      ├─ css/                           # JavaFX stylesheets
      └─ i18n/                          # message bundles (pt-BR / en)
```

> **Build note:** combining JavaFX + the Spring Boot fat-jar plugin needs care (module-path vs class-path for JavaFX). Recommended packaging: `jlink`/`jpackage` producing a native installer (MSI/DEB) that bundles the JRE + JavaFX modules. Document the chosen `maven-shade`/`spring-boot-maven-plugin` + `javafx-maven-plugin` combination during setup.

---

## 12. Cross-Cutting Concerns
- **Validation:** Jakarta Bean Validation on DTOs; surfaced in the form UI.
- **Error handling:** central handler translating DB/constraint/security exceptions into user-friendly dialogs; never leak stack traces to the UI.
- **Logging:** Logback to a rotating file in the user's app-data dir + console in dev.
- **i18n:** message bundles (column labels look Portuguese — provide pt-BR primary).
- **Transactions:** service-level; UI never manages transactions.
- **Connection resilience:** detect lost DB2/AD connection; show reconnect prompt.

---

## 13. Testing Strategy
- **Unit:** services with mocked repositories.
- **Integration:** repositories against **Testcontainers DB2** (`icr.io/db2_community/db2`), verifying entity mappings and the Graph↔DataNumber relationship.
- **Security:** authentication/authorization tests with an embedded LDAP (e.g. `UnboundID`/`spring-security-test`).
- **UI (optional):** TestFX smoke tests for critical CRUD flows.
- **Manual UAT checklist** per module (create/edit/delete/validation/permissions).

---

## 14. Milestones
1. **M1 — Skeleton:** Spring Boot + JavaFX bootstrap in one JVM; DB2 connectivity; empty main window. 
2. **M2 — Read-only:** entities + repositories + list views for all four modules; confirm `CD_NUMERO` constraint/generation.
3. **M3 — CRUD:** create/edit/delete forms + validation; Graph↔DataNumber master-detail.
4. **M4 — Security:** LDAP/AD login, role mapping, method security, UI gating.
5. **M5 — Polish & package:** i18n, error handling, `jpackage` installer, UAT.

---

## 15. Open Questions
- **Q1 — `CD_NUMERO` constraint:** Is there an enforced PK/unique constraint on `LNI_NUMERO_UEM.CD_NUMERO`, or just a plain column? Confirm values are unique before relying on it as the JPA identity.
- **Q2 — `VL_VALOR` type:** OK to use `BigDecimal` instead of `Double` for the `DECIMAL(10,2)` money field? (Recommended.)
- **Q3 — AD details:** domain, `ldaps` URL, base DN, group→role mapping.
- **Q4 — DB2 platform:** LUW or z/OS? (dialect + pagination + driver specifics)
- **Q5 — ID generation:** Are `CD_GRAFICO` / `CD_NUMERO` / `CD_PROJETO` / `CD_SUSTENTABILIDADE` DB-generated (identity/sequence) or app-assigned?
- **Q6 — ODS (`TP_ODS`):** Is there a fixed code list (UN SDG 1–17) to render as labels/enum, or free integer?
- **Q7 — Concurrency:** Is optimistic locking (`@Version`) needed? None of the tables currently shows a version column.
```
