# Quality checklist (required before every commit)

Check **all 11 points** before every commit. Automated limits are enforced by `scripts/check-quality.sh`. Architecture and behavior still need a human/agent review of this file.

Run:

```bash
./scripts/check-quality.sh
```

Then confirm the points below.

---

## 1. No god classes

A class does **one** job in **one** bounded context. Do not mix UI, SQLDelight, matching, scoring, and ads in one type.

- Split when a type approaches **300–500 lines**, or sooner if it has mixed reasons to change.
- Data tables (fixture lists) may be split into dedicated files; they are not an excuse for a behavioral god object.
- ViewModels map one screen. Use cases orchestrate. Domain services do not know Compose.

## 2. DDD

- Group by **bounded context** (catalog, ingredients, hazards, evaluation, scanning, preferences, ads, i18n), not by “all repositories in one folder of implementations used as the API”.
- **Domain** has no Compose, SQLDelight generated types, Android, or iOS APIs.
- **Application** use cases depend on **domain ports** (interfaces), not SQL classes.
- **Data** implements ports. UI depends on use cases and ports, never on `Sql*` types.
- Entities/value objects stay immutable where practical.

## 3. SOLID

- **S:** one reason to change per class; one thing per method.
- **O:** new hazard rules, matchers, or repositories by adding types, not by growing `when` blobs in UI.
- **L:** implementations honor port contracts (`Outcome`, thread rules).
- **I:** small ports (`ProductRepository`, `PreferencesStore`, `ScanHistoryRepository`, `CatalogGateway`).
- **D:** depend on abstractions; bind implementations in Koin only.

## 4. Best practices and mistakes (after the final write)

Re-read the diff. Look for:

- Unused imports, FQCN clutter, duplicated mapping.
- `catch (Throwable)` / swallowing errors / ignoring `CancellationException`.
- Domain calling Compose or SQLDelight.
- Hard-coded user-facing English/Polish in Kotlin (use `composeResources`).
- Race-prone shared mutable state without a mutex or single-thread dispatcher.

## 5. Class size

**Maximum 500 lines** per Kotlin file/class. Prefer under 300 for behavior types. `scripts/check-quality.sh` fails the commit if any `*.kt` file exceeds 500 lines.

## 6. Method size

**Maximum 250 lines** per method. Prefer **under 50**. If a method is over ~50 lines, it is probably doing more than one thing — extract.

## 7. One method, one thing

No mixing “talk to SQLite” with “score a formula” with “update navigation”. Name methods after the single action (`readHazards`, `evaluateAndOpen`, `showFailure`).

## 8. Work off the main thread

- SQLite: `catalogDatabase` / `userDatabase` (each `limitedParallelism(1)`).
- Matching, fuzzy OCR, index assemble: `computation` (`Dispatchers.Default`).
- Process-lifetime jobs: `ApplicationScope` on `io`, with a `SupervisorJob` and `CoroutineExceptionHandler`.
- ViewModels may start on Main; they must **not** do catalog I/O or evaluation on Main. Compose must not call SQL or `evaluate` during composition.
- Do not hold the SQLite dispatcher while doing CPU-heavy assemble/match.

## 9. ≥ 90% iOS and Android compatibility

Keep **both** floors unless product explicitly raises them:

| Platform | Floor | Why |
| --- | --- | --- |
| Android `minSdk` | **26** (Android 8.0) | Well above 90% of active devices; do not use APIs without compat or a higher `minSdk`. |
| iOS deployment | **15.3** | Covers the App Store install base; prefer `arm64`, not `armv7`. |

Shared code must stay in `commonMain` or honest `expect`/`actual`. No Android-only calls in common. No iOS-only Foundation types in common.

`compileSdk` / `targetSdk` may track current Android; that does not raise `minSdk`.

## 10. Multithreading is safe

- **Never** catch `CancellationException` (do not `catch (Throwable)` either). Rethrow cancellation, then catch `Exception`.
- Catalog SQLite and user SQLite are **separate** single-thread dispatchers. Do not touch a SQLDelight database off its dispatcher.
- Shared mutable session/preferences: `Mutex`. In-memory catalog index is immutable after assemble.
- Parallel `async` work must be **read-only** on shared structures (`ParallelMapper` + immutable matcher maps).
- Cancel in-flight UI jobs when the user starts a new action (`Job.cancel()`), and still clear `busy` in `finally`.
- Completing `CompletableDeferred` with `Outcome` (not process-killing exceptions) for expected catalog failures.

## 11. Robust, verbose error handling

- Failures are `AppFailure` + `Outcome` (operation code + exception type/message chain).
- Log with `AppLog` (Android `Log`, iOS `NSLog`, JVM stderr).
- UI shows a **title** (i18n) and the **verbose** `operation — detail` line. Offer **Retry** where it can help.
- Do not crash the scan flow on a history-write failure after a successful evaluation; log a warning.
- Catalog bootstrap failures surface on the next evaluation/preferences load, not as an uncaught coroutine crash.
- User-facing copy stays in XML; details may include exception text for support.

---

## Commit gate

1. `./scripts/check-quality.sh`
2. Mentally walk 1–11 against the diff.
3. `./gradlew :shared:jvmTest` (and `:androidApp:assembleDebug` if Android/UI/Gradle changed).
4. Only then `git commit`.
