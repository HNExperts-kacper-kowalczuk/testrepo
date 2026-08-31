# After phases 21–23: next work

Phases **21–23** in [plan-after-twenty.md](plan-after-twenty.md) are **shipped** on `main` (PRs **#32–#34**). This document is the next slice: **24–25 shipped** (#35–#36); **26 is this PR**.

**Each phase is one PR.** Stack on `main` after the previous phase merges. Suggested branches: `cursor/phase-24-ruleset-changelog-4039` … `cursor/phase-26-theme-override-4039`. Do not combine two phases in one PR.

Related: [further-additions.md](further-additions.md), [plan.md](plan.md), [quality-checklist.md](quality-checklist.md).

**Do not mix extra scores into the hazard traffic light. Do not scrape retailers. Do not add accounts, cloud OCR, or medical modes.**

---

## Current baseline (after PR #34)

| Area | Today |
| --- | --- |
| Settings catalog | Stamp + “what this includes” notes. No version-stamped **history** |
| Changelog | Called out in [further-additions.md](further-additions.md) §6; explicitly **out** of phases 18 and 22 (no SQLite table, no live CosIng feed) |
| Store listings | Play/App Store copy still described only the microplastics chip before **phase 25** |
| Theme | System / light / dark in Preferences (this PR). `RatingColors` stay fixed |

---

## Phase 24 — Ruleset changelog in Settings

**PR:** `cursor/phase-24-ruleset-changelog-4039`  
**Outcome:** Settings shows a short history of this app’s catalog rules so the offline catalog feels current — without a fabricated “40 comments updated” count and without a live CosIng feed.

### 24.1 Behaviour

- Code-side list (`RulesetChangelog`), newest first. **Not** a SQLite changelog table.
- Packed-catalog line uses the device `CatalogMeta.catalogVersion`.
- Overlay lines (allergen/microplastics, sun/children/pregnancy, animal-derived) say they apply **when the catalog loads**.
- EN/PL keys. Stamp and existing notes stay.
- One disclaimer: this is this app’s ruleset history, not a live official update feed.

### 24.2 Done when

- Settings catalog section shows the changelog under the existing notes.
- Missing catalog meta omits the packed-snapshot line; overlay lines still show.
- Packed line repeats the stamp’s catalog version, not a guessed comment count.
- `:shared:jvmTest` and quality script green.

**Out of this PR:** live CosIng “what changed” feed; SQLite changelog table; store listing rewrite (25); theme override (26).

---

## Phase 25 — Store listing + in-app copy pass

**PR:** `cursor/phase-25-store-copy-4039`  
**Depends on:** 24 (shipped as **#35**; optional for this copy).  
**PR:** **#36** (shipped).  
**Outcome:** Play/App Store docs mention sun-caution / children / pregnancy notes and the animal-derived chip, not only microplastics. EN/PL. No traffic-light change.

In-app Settings notes and result chips already cover those tags (phases 22–23). This PR updates store listings and related sideload/catalog docs.

### 25.1 Done when

- `docs/store/play-en.md`, `play-pl.md`, `app-store-en.md`, and `app-store-pl.md` mention both extra chips and that sun/children/pregnancy notes are incomplete, not diagnoses, and not the traffic light.
- Quality script green.

**Out of this PR:** theme override (26); changing `DangerLevel`.

---

## Phase 26 — In-app theme override

**PR:** `cursor/phase-26-theme-override-4039` (this PR)  
**Depends on:** 25 (shipped as **#36**).  
**Outcome:** Light / dark / system in Preferences. `RatingColors` stay fixed. No dynamic colour on ratings.

### 26.1 Behaviour

- `ThemePreference` (`system` / `light` / `dark`) on `user_profile`, migrated for existing installs.
- `ThemeSession` publishes the live choice; `CosmeticsTheme` resolves dark surfaces from that plus the system setting.
- Device reset keeps appearance the same way it keeps locale.
- EN/PL keys. Filter chips in Settings, extracted from `PreferencesScreen`.

### 26.2 Done when

- Preferences can pin light, pin dark, or follow the system; the choice persists locally.
- Rating badges still use `RatingColors` (green/red do not follow the scheme).
- Device reset does not revert a pinned dark/light theme.
- `:shared:jvmTest` and quality script green.

**Out of this PR:** Material You dynamic colour on ratings; per-screen themes; foldables.

---

## Explicitly out of this slice

| Item | Why it waits |
| --- | --- |
| Production AdMob IDs, sync URLs | Uncommitted `local.properties` / XCConfig |
| Real Remove-ads IAP | Store billing; keep `NoOpBillingPort` |
| Vegan/cruelty/palm/endocrine as **safety scores** | Must never enter the traffic light |
| Palm / cruelty-free / endocrine **chips** | P3; incomplete brand- or name-level data |
| Gzip rewrite of packed tags | Needs gitignored `catalog/ingest/` |
| Foldables, CJK OCR | Optional / other markets |
| Accounts, cloud OCR, retailer scrape, medical modes | Product principles |

---

## Implementation order (one PR each)

```
24 ruleset changelog in Settings   ← shipped #35
25 store listing copy pass         ← shipped #36
26 in-app theme override           ← this PR
```

---

## Testing (every phase)

- `./scripts/check-quality.sh` then `:shared:jvmTest`
- EN/PL key parity; files ≤ 500 lines; rethrow `CancellationException`; no `catch (Throwable)`
- Do not call live OBF/CosIng from unit tests
- Do not assemble or commit `releases/*.apk` unless explicitly asked
- Walk [quality-checklist.md](quality-checklist.md) points 1–11 before commit
