# DataLNI

Desktop CRUD application for the **PLD** schema on **IBM DB2**, built as a single JVM that
combines a **Spring Boot 4.0.6** backend (service + persistence) with a **JavaFX** UI.
Authentication is delegated to **LDAP / Active Directory** in production; a development
profile uses in-memory users. See [SPEC.md](SPEC.md) for the full specification.

## Requirements

- **JDK 21** (the build/run toolchain; Spring Boot 4 needs 17+).
- **Maven 3.8+**.
- A reachable **DB2** instance (the app validates mappings against a live schema; it does
  not create tables).
- For production login: a reachable **Active Directory** over `ldaps`.

## Running (development)

The `dev` profile (active by default) uses **in-memory users** but a **real development
DB2** supplied via environment variables:

```bash
export DB2_URL="jdbc:db2://<dev-host>:50000/<db>:currentSchema=PLD;"
export DB2_USER="..."
export DB2_PASSWORD="..."

export JAVA_HOME="$HOME/.sdkman/candidates/java/21.0.8-oracle"
mvn javafx:run
```

`mvn javafx:run` resolves the platform-specific JavaFX natives for you. A graphical
display is required (the app is a desktop GUI).

**Dev users** (username / password → role):

| User     | Role         | Capability                |
|----------|--------------|---------------------------|
| `admin`  | `LNI_ADMIN`  | full CRUD                 |
| `editor` | `LNI_EDITOR` | create / update / delete  |
| `viewer` | `LNI_VIEWER` | read-only (edit hidden)   |

## Running (production)

```bash
export DB2_URL=... DB2_USER=... DB2_PASSWORD=...
export LDAP_URL="ldaps://ad-host:636"
export LDAP_DOMAIN="corp.example.com"
export LDAP_BASE_DN="DC=corp,DC=example,DC=com"

java -Dspring.profiles.active=prod -jar target/datalni.jar
```

Edit the AD-group → app-role mapping in `application-prod.yml`
(`LNI_ADMINS → LNI_ADMIN`, etc.).

## Build, test, package

```bash
mvn test            # unit + H2 mapping slice + method-security tests (15)
mvn package         # builds target/datalni.jar (fat jar; main = Launcher)
mvn -Pit verify     # also enables the opt-in DB2 Testcontainers IT (needs Docker)
```

The `GraphRepositoryDb2IT` integration test is `@Disabled` by default (the IBM DB2 image
is large/slow); remove the annotation to exercise the real DB2 insert path.

## Native packaging (jpackage)

```bash
mvn -Pjpackage -DskipTests package        # -> target/dist/DATALNI (self-contained app image)
```

The `jpackage` profile lays the app jar + all dependencies (including the JavaFX
**natives for the build OS**) flat in an input dir and runs `jpackage`, bundling a Java 21
runtime — so the result needs **nothing pre-installed** on the user's machine.

**Per-OS:** `jpackage` cannot cross-compile — run it **on each target OS** (the OS profiles
auto-pick the right JavaFX natives). Choose the artifact type with `-Djpackage.type` (default
`APP_IMAGE`, which needs no extra tooling):

| OS | Command | Extra tooling |
|----|---------|---------------|
| Linux | `mvn -Pjpackage -DskipTests -Djpackage.type=DEB package` | `dpkg`, `fakeroot` (or `RPM` → `rpm-build`) |
| Windows | `mvn -Pjpackage -DskipTests -Djpackage.type=MSI package` | WiX Toolset (also for `EXE`) |
| macOS | `mvn -Pjpackage -DskipTests -Djpackage.type=DMG package` | Xcode CLT (also for `PKG`) |

To produce installers for all three from one place, run this per-OS in a CI matrix
(e.g. GitHub Actions `runs-on: [ubuntu, windows, macos]`).

## How the SPEC open questions were resolved

| Q  | Topic                | Decision in this build |
|----|----------------------|------------------------|
| Q1 | `CD_NUMERO` PK       | Treated as the JPA `@Id` of `DataNumber`. |
| Q2 | `VL_VALOR` type      | `BigDecimal` (`DECIMAL(10,2)`). |
| Q3 | AD details           | Externalised to `app.ldap.*` / env vars (`application-prod.yml`). |
| Q4 | DB2 platform         | LUW assumed — `org.hibernate.dialect.DB2Dialect`. |
| Q5 | ID generation        | Application-assigned via `MaxIdGenerator` (`MAX(id)+1`), wired with `@GenericGenerator`. |
| Q6 | `TP_ODS`             | Free integer; labelled for display via `SdgCatalog` (UN SDG 1–17, pt-BR). |
| Q7 | Concurrency          | No `@Version` (no version columns in the schema). |

## Architecture notes

- **One JVM, direct bean calls.** `Launcher` (plain `main`) → `DataLniFxApp`
  (`javafx.application.Application`) boots Spring headless in `init()`; FXML controllers
  are resolved from Spring via the controller factory (`SpringFxmlLoader`).
- **Threading.** All service/DB calls run off the FX thread through `AsyncRunner`, backed
  by a `DelegatingSecurityContextExecutor` so the logged-in user's `SecurityContext`
  propagates to worker threads and `@PreAuthorize` sees it.
- **Layering.** UI → `@Service` (`@Transactional`, `@PreAuthorize`, MapStruct DTO mapping,
  Bean Validation) → Spring Data repositories (with `JpaSpecificationExecutor` for search).
- **Theme.** AtlantaFX Primer Light by default, Primer Dark toggle (View menu).
- **`MaxIdGenerator` is DB2-specific** (`SELECT ... WITH RS USE AND KEEP EXCLUSIVE LOCKS`),
  so insert-path tests run against DB2 (Testcontainers); the fast H2 slice test only reads.
```
