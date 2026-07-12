# 🔬 SurMaya QA & Certification Lab

**Document Reference**: SRMY-QA-LAB-README-01  
**Establishment Date**: July 11, 2026  
**Status**: ACTIVE  
**Governance Authority**: Chief Technology Officer & SurMaya QA Board  
**Target Version**: SurMaya AI v1.0 RC2  

---

## 🏛️ 1. Mission & Purpose

The **SurMaya QA & Certification Lab** is the centralized, permanent governance repository established under **CTO Directive 010 (Release Lock)**. Its mission is to bridge the gap between development reporting and real-world execution by housing all audit artifacts, device matrices, performance profiles, and testing guidelines in a single, unalterable location.

As we transition from **v1.0 RC2** into **Internal Device Acceptance Testing**, the Lab guarantees that every build meets our strict architectural, visual, and performance standards before being promoted to **v1.0 Production**.

---

## 📂 2. Lab Directory Structure

All verification documents, benchmarks, and device-testing trackers are organized as follows:

```text
/docs/qa_certification_lab/
  ├── README.md                      <-- This Guide (General Governance)
  ├── ACCEPTANCE_TEST_REPORTS.md     <-- Physical Device Tracker & Templates
  ├── DEVICE_COMPATIBILITY_MATRIX.md <-- OS Compatibility Tiers & Features
  ├── PERFORMANCE_BENCHMARKS.md      <-- Processing Latencies & Buffer Benchmarks
  ├── REGRESSION_TEST_REPORTS.md     <-- Protocols to prevent drift during patch updates
  └── CHANGELOG.md                   <-- Full development history (Inception to v1.0 RC2)
```

---

## 🔒 3. Release Governance Policy

To preserve the absolute integrity of **SurMaya AI**, the codebase is placed under a strict **Engineering Lock**. No changes are permitted except for those matching the allowed categories:

### Allowed Categories
* 🐛 **Bug Fixes**: Rectifying functional defects or state issues.
* 💥 **Crash Fixes**: Mitigating any runtime exceptions or ANRs.
* ⚡ **Performance Optimization**: Reducing latency, processing overhead, and memory footprints.
* 🔋 **Battery Optimization**: Minimizing CPU cycles in active and idle audio states.
* 📶 **Device Compatibility**: Fixing layout clipping or platform-specific driver incompatibilities.
* 🛡️ **Security Patches**: Updating SDKs and dynamic library links to prevent vulnerabilities.

### Forbidden Categories
* ❌ **New Features**: No unrequested features or menus.
* ❌ **UI Redesigns**: Layout structure and Material 3 styles are frozen.
* ❌ **Database Schema Alterations**: Room schemas are frozen to prevent migration crashes.
* ❌ **Runtime API Changes**: AIRE Runtime v2.0 signal-processing pipelines are locked.

---

## 🧪 4. Continuous Integration & Local Verification

Before submitting any bug fix or optimization for QA Certification, developers must run local verification tests:

### Running Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```
*Executes all underlying unit tests, DSP math verifications, and Context-resource assertions.*

### Verifying Screenshot Tests (Roborazzi)
```bash
gradle :app:verifyRoborazziDebug
```
*Ensures visual elements, screen density margins, and Edge-to-Edge components remain pixel-perfect.*

### Recording Reference Screenshots
```bash
gradle :app:recordRoborazziDebug
```
*Only to be run when a verified visual bug has been fixed and approved by the QA Board.*

---

## 🤝 5. Verification Accountability

While the automated CI/CD pipeline and JVM/Robolectric test suites provide immediate code-level feedback, **SurMaya AI** recognizes that physical device verification is the ultimate production gate. Every entry in the `ACCEPTANCE_TEST_REPORTS.md` matrix must be filled with real device results, ensuring an objective, audited transition to **SurMaya AI v1.0 Production**.

---
*Certified for implementation by:*  
**SurMaya QA & Certification Lab Board**  
**Sri Itnaa Co-Founding Engineering Division**  
**Founder: PRADEEP SINGH**  
