# After phases 21–23: next work

Phases **21–23** in [plan-after-twenty.md](plan-after-twenty.md) are **shipped** on `main` (PRs **#32–#34**). This document is the next slice.

**Each phase is one PR.** Stack on `main` after the previous phase merges. Suggested branches: `cursor/phase-24-ruleset-changelog-4039` … `cursor/phase-26-theme-override-4039`. Do not combine two phases in one PR.

Related: [further-additions.md](further-additions.md), [plan.md](plan.md), [quality-checklist.md](quality-checklist.md).

**Do not mix extra scores into the hazard traffic light. Do not scrape retailers. Do not add accounts, cloud OCR, or medical modes.**

---

## Current baseline (after PR #34)

| Area | Today |
| --- | --- |
| Settings catalog | Stamp + “what this includes” notes. No version-stamped **history** |
| Changelog | Called out in [further-additions.md](further-additions.md) §6; explicitly **out** of phases 18 and 22 (no SQLite table, no live CosIng feed) |
| Store listings | `docs/store/play-en.md` still mentions only the microplastics chip |
| Theme | Follows the system; `RatingColors` fixed. No in-app light/dark override |

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

## Phase 25 — Store listing + in-app copy pass (later)

**PR:** `cursor/phase-25-store-copy-4039`  
**Depends on:** 24 optional.  
Play/App Store docs and any in-app lines that still describe only the microplastics chip should mention sun-caution / children / pregnancy notes and the animal-derived chip. EN/PL. No traffic-light change.

---

## Phase 26 — In-app theme override (optional, last)

**PR:** `cursor/phase-26-theme-override-4039`  
Light / dark / system in Preferences. `RatingColors` stay fixed. No dynamic colour on ratings.

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
24 ruleset changelog in Settings   ← leftover from 18/22; do this first
25 store listing copy pass
26 in-app theme override (optional last)
```

---

## Testing (every phase)

- `./scripts/check-quality.sh` then `:shared:jvmTest`
- EN/PL key parity; files ≤ 500 lines; rethrow `CancellationException`; no `catch (Throwable)`
- Do not call live OBF/CosIng from unit tests
- Rebuild `releases/inci-scan-debug.apk` when UI or catalog packing changes
- Walk [quality-checklist.md](quality-checklist.md) points 1–11 before commit
