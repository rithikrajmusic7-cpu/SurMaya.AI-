# SurMaya AI - Testing & Verification Manual

This document details the testing architecture, practices, and guidelines of the SurMaya AI Music Operating System. It outlines our JVM, Robolectric, and Roborazzi testing frameworks.

---

## 🏛️ Testing Architecture & Standards

To facilitate extremely rapid prototyping and ensure architectural stability, SurMaya AI implements a comprehensive, lightweight test execution matrix. We focus heavily on **local JVM testing** rather than heavy, slow Android emulators.

```
                  [Developer Code Edit]
                            │
                            ▼
           +─────────────────────────────────+
           │    Local Unit Tests (JVM)       │  ◄── Ultra fast (<1s)
           │    (Business logic, engines)    │
           +────────────────┬────────────────+
                            │
                            ▼
           +─────────────────────────────────+
           │    Robolectric Integration      │  ◄── Mocked Android Framework (~3s)
           │    (ViewModels, SQL persistence)│
           +────────────────┬────────────────+
                            │
                            ▼
           +─────────────────────────────────+
           │    Roborazzi Screenshot Tests   │  ◄── Visual regression checks (~5s)
           │    (Jetpack Compose UI Layouts) │
           +─────────────────────────────────+
```

---

## 🧪 1. Local Unit Testing (Pure JVM)

All domain logic (algorithmic engines, phoneme mapping, ornamentation injection, mathematical oscillator waves computations) is written in pure Kotlin and resides in the **Domain** and **Data** layers.

### Run Local Unit Tests
To run all standard unit and Robolectric tests, execute:
```bash
gradle :app:testDebugUnitTest
```

### Best Practices
- **Mock Interfaces Directly**: Avoid importing heavy frameworks. Since repositories and engines rely on pure Kotlin interfaces, mock them directly or use lightweight mock implementations in test constructors.
- **No Android Dependencies**: Keep files inside domain test folders strictly free of Android SDK imports (`android.os.*`, etc.).

---

## 🤖 2. Robolectric Integration Testing

For classes that interact directly with the Android Framework (such as `Context`, `SharedPreferences`, or SQLite databases via SQLite Room), we utilize **Robolectric** to run tests locally on the JVM without booting an emulator.

### Example Configuration
Robolectric tests reside under `src/test/` alongside standard unit tests, annotated with `@RunWith(RobolectricTestRunner::class)` and configured to simulate modern Android platforms.

---

## 📸 3. Roborazzi Screenshot Testing

To prevent unexpected UI shifts, layout regressions, or component design breaking during rapid iterations, we utilize **Roborazzi** for fast, local screenshot validation.

### Core Commands

#### A. Record Reference Images
When making intentional visual or layout adjustments to Compose screens, record a new set of reference images:
```bash
gradle :app:recordRoborazziDebug
```

#### B. Verify Layouts
To check current Compose rendering against saved reference screenshots (for CI or pull request audits):
```bash
gradle :app:verifyRoborazziDebug
```

### Key Rules
- **Interactive Component TestTags**: Always specify `.testTag("target_tag")` on clickable cards, buttons, input fields, and lists so Roborazzi and UI test runners can target components reliably.
- **Isolate Threading**: Ensure all animations, loading states, or continuous progress bars are mocked or set to fixed states before snapshots to prevent screenshot non-determinism.

---

*SurMaya AI Engineering Board - Approved for Integration Phase.*
