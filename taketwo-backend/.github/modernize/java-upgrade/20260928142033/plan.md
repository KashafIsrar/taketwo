# Upgrade Plan: taketwo-backend (20260928142033)

- **Generated**: 2026-09-28 14:20:33
- **HEAD Branch**: main
- **HEAD Commit ID**: N/A

## Available Tools

**JDKs**
- JDK 17: not available (baseline will be skipped)
- JDK 25: d:\Personal Projects\TAKETWO\.tools\jdk-25\jdk-25.0.2\bin (required by upgrade step 3)

**Build Tools**
- Maven 3.9.15: d:\Personal Projects\TAKETWO\.tools\maven-3.9.15\maven-3.9.15\bin\mvn (installed for Java 25 compatibility)

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

- Upgrade to the latest LTS Java runtime: Java 25.
- Keep the change scope focused on the Java toolchain and compatibility validation; avoid unrelated framework churn.
- Prefer the installed JDK 25 and Maven 3.9.15 for all compile and test verification.

## Options

- Working branch: appmod/java-upgrade-20260928142033
- Run tests before and after the upgrade: true

## Upgrade Goals

- Java runtime: 25

## Technology Stack

| Technology/Dependency | Current | Min Compatible Version | Why Incompatible |
| ---------------------- | ------- | ---------------------- | ---------------- |
| Java | 17 | 25 | User requested Java 25 LTS |
| Spring Boot | 3.3.4 | 3.3.4 | Already on a Java 17+ compatible release; compatibility will be validated on Java 25 |
| Maven | not installed | 3.9.15 | Required for Java 25 build/test execution |
| JJWT | 0.12.6 | 0.12.6 | No upgrade required for runtime target change |
| PostgreSQL driver | managed by Spring Boot | current managed version | No direct version conflict expected for Java 25 upgrade |

## Derived Upgrades

- Java 25 requires a JDK 25 toolchain and Maven 3.9.15+ for reliable build/test execution.
- The project already targets Java 17 via `java.version`, so the runtime upgrade is a direct property change plus validation on the new JDK.
- No Spring Boot major-version jump is required at this time because the project is already on Spring Boot 3.x and will be validated on Java 25 before any further framework change is considered.

## Impact Analysis

### Subsection: Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
|------|------------|---------|--------|--------|--------|
| pom.xml | java.version | 17 | upgrade | 25 | User requested Java 25 LTS runtime |
| pom.xml | Maven build environment | not available | install/ensure | 3.9.15 | Required for Java 25 build/test execution |

### Subsection: Source Code Changes

| File | Location | Current | Required Change | Reason |
|------|----------|---------|----------------|--------|
| None expected | project-wide compile tree | Java 17 source level | Validate against Java 25 compiler and fix any incompatibilities | Java version change only; no framework namespace migration required |

### Subsection: Configuration Changes

| File | Property/Setting | Current | Required Change | Reason |
|------|------------------|---------|----------------|--------|
| None expected | runtime configuration | N/A | Keep existing config as-is until Java 25 compilation/test validation identifies an issue | No direct config property rename is required for a Java-only runtime upgrade |

### Subsection: CI/CD Changes

| File | Location | Current | Required Change |
|------|----------|---------|----------------|
| None detected | project root | no pipeline files present | No CI/CD update required for this workspace |

### Subsection: Risks & Warnings

- **Java 25 compatibility risk**: The project is currently compiled with Java 17. A direct runtime jump can expose source compatibility issues or hidden reflection problems that are not visible under Java 17. **Mitigation**: validate compilation and full tests on JDK 25 immediately after updating `java.version`.
- **Build tool availability risk**: Maven is not installed in the current environment, which would block verification. **Mitigation**: install Maven 3.9.15 and run all verification with the JDK 25 toolchain explicitly set.

## Upgrade Steps

- Step 1: Setup Environment
  - **Rationale**: The project cannot be compiled or tested on Java 25 without a compatible JDK and Maven installation.
  - **Changes to Make**: Install JDK 25 and Maven 3.9.15; verify availability.
  - **Verification**: `java -version` and `mvn -version` using the installed paths; expected result: Java 25 runtime and Maven 3.9.15 detected.

- Step 2: Setup Baseline
  - **Rationale**: The project’s current Java JDK is not available in the environment, so a pre-upgrade baseline cannot be executed here without a local Java 17 toolchain.
  - **Changes to Make**: Skip compilation and tests for the current baseline due to missing Java 17.
  - **Verification**: Baseline step is recorded as skipped because the current JDK is unavailable; expected result: upgrade proceeds with Java 25 validation.

- Step 3: Upgrade Java Runtime to 25
  - **Rationale**: This is the core change required by the user request and is the minimal change consistent with the project’s current Spring Boot 3.x baseline.
  - **Changes to Make**: Apply all Dependency Changes in `pom.xml` by setting `java.version` to `25`; then rebuild with the JDK 25 toolchain to catch any compile-time incompatibilities.
  - **Verification**: `JAVA_HOME="d:\Personal Projects\TAKETWO\.tools\jdk-25\jdk-25.0.2" "d:\Personal Projects\TAKETWO\.tools\maven-3.9.15\maven-3.9.15\bin\mvn" clean test-compile -q`; expected result: compilation success on Java 25.

- Step 4: CVE Validation & Fix
  - **Rationale**: Direct dependencies should be checked for known vulnerabilities after the runtime upgrade to ensure security posture is maintained.
  - **Changes to Make**: Extract the direct dependency list from Maven, run a CVE scan, and only adjust dependency versions if vulnerabilities are reported.
  - **Verification**: Maven dependency listing plus CVE scan results; expected result: no unresolved direct-dependency CVEs remain.

- Step 5: Final Validation
  - **Rationale**: The Java 25 migration is only complete when the project builds and all tests pass under the target runtime.
  - **Changes to Make**: Fix any Java 25 compatibility issues surfaced in the full test run; keep changes minimal and limited to actual incompatibilities.
  - **Verification**: `JAVA_HOME="d:\Personal Projects\TAKETWO\.tools\jdk-25\jdk-25.0.2" "d:\Personal Projects\TAKETWO\.tools\maven-3.9.15\maven-3.9.15\bin\mvn" clean test -q`; expected result: 100% pass rate on Java 25.
