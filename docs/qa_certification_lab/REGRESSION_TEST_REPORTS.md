# 🛡️ SurMaya AI - Regression Test Reports

**Document Reference**: SRMY-QA-REGRESS-105  
**Status**: ACTIVE  
**Last Updated**: July 11, 2026  
**Audience**: Release Engineers & Maintenance Developers  

---

## 🏛️ 1. Purpose & Scope

Under **CTO Directive 010 (Release Lock)**, the architecture, database schema, branding, and core UI of **SurMaya AI** are frozen. However, as the application moves into maintenance and production, small patches will be required (e.g., v1.0.1, v1.0.2) to fix edge-case bugs, optimize performance, or maintain security dependencies.

This document establishes the **Regression Testing Protocol** to guarantee that future updates do not introduce regressions into the certified stable core.

---

## 🔒 2. No-Drift Architectural Rules

Every maintenance patch must comply with these four immutable rules. Any violation will result in immediate rejection by the QA Certification Lab:

1. **No Schema Modifications**: The Room SQLite database schema version must remain locked. If schema updates are absolutely mandatory to address a P1 security bug, a migration plan with verified testing must be signed off by the CTO.
2. **No Core Audio API Modifications**: The JNI interfaces, buffer formats, and sample rates inside **AIRE Runtime v2.0** must not be modified.
3. **No Unsolicited Dependencies**: No external SDK or third-party libraries can be added during a patch update.
4. **No UI Styling Alterations**: The visual theme, colors, typography, and edge-to-edge system integrations are frozen.

---

## 📋 3. Regression Testing Protocol (Pre-Release Checklist)

Before any minor version patch (e.g., `v1.0.x`) is promoted from a development branch to production, release engineers must execute and log this checklist:

### A. Core Compilation & Binary Integrity
- [ ] **Clean Build**: Build the project using a clean workspace (`gradle assembleDebug`) to verify no residual compiler cached configurations cause build differences.
- [ ] **Linter Review**: Run the project linter to confirm zero new syntax warnings or import errors are introduced.
- [ ] **Size Analysis**: Check that the generated APK does not exceed the target threshold limit of **30 MB**.

### B. Automated Test Preservation
- [ ] **Robolectric Suite**: Run `:app:testDebugUnitTest`. Confirm that 100% of JVM unit tests pass.
- [ ] **Roborazzi Screenshot Match**: Run `:app:verifyRoborazziDebug`. Confirm that 100% of screenshot comparisons match the reference images without visual drift.

### C. Database & Save Compatibility
- [ ] **Upgrade Test**: Take an active database containing multiple saved projects from v1.0 RC2. Install the new patch directly over it. Verify that old projects open and load without SQL exceptions or data truncation.
- [ ] **Clean Workspace Init**: Install the new patch on a fresh partition. Verify that the SQLite database compiles and initializes its tables cleanly.

---

## 📊 4. Regression Test Run Log

Use this register to document the verification of minor version patches:

| Patch Version | Commit Hash | Date | Tester | Automated Tests | Upgradability | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **v1.0.0 (Gold)**| | | | | | |
| **v1.0.1** | | | | | | |
| **v1.0.2** | | | | | | |
| **v1.0.3** | | | | | | |

---

## 🚦 5. Certification Gate Sign-off

No patch will be deployed to the internal team or production channel without a signed verification record registered in this directory.

```text
The undersigned certifies that this patch release has undergone complete regression testing 
and introduces zero architectural, visual, or functional drift.

Certified by:
___________________________
Lead Release Engineer

___________________________
Director of Quality, Sri Itnaa
```
