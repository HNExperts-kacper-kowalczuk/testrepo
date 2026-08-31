# After phases 1–15: next work

**Status: shipped** (PRs **#25–#30**). Phases **1–5** in [plan-next-phases.md](plan-next-phases.md) and **6–15** in [plan-further-improvements.md](plan-further-improvements.md) were already shipped. This document is the completed 16–20 slice. Further work: [plan-after-twenty.md](plan-after-twenty.md).

**Each phase is one PR.** Stack on `main` after the previous phase merges. Suggested branches: `cursor/phase-16-catalog-tags-4039` … `cursor/phase-20-dark-scheme-4039`. Do not combine two phases in one PR. If a phase grows past a focused review, split it *before* opening the PR, and update this file.

Related: [further-additions.md](further-additions.md), [plan.md](plan.md), [quality-checklist.md](quality-checklist.md), [catalog/README.md](../catalog/README.md).

**Do not mix extra scores into the hazard traffic light. Do not scrape retailers. Do not add accounts, cloud OCR, or medical modes.**

---

## Current baseline (after PR #24)

| Area | Today |
| --- | --- |
| Product | Offline barcode + INCI OCR, usage confirm, presets, shelf/compare/alternatives, share text+image, wipe, microplastics **chip** |
| Packed DB | `catalog.sqlite.gz` (~17k products, ~36k CosIng ingredients), checksum `2026.08-eu-pl`, packed **2026-08-19** |
| Tag indexes | `EuLabelledAllergenIndex` and `MicroplasticIndex` run at **ingest** (`CosingAssembler`) and on **fixtures** |
| Shipped tags | Packed CosIng `POLYETHYLENE` / `GERANIOL` / `ACETYL CEDRENE` have **empty** allergen/microplastic tags. `ALLERGEN_26` is only on four fixture rows. `MICROPLASTIC` count in the gzip is **0**. |
| Ads / billing | Debug Google sample IDs; release IDs from uncommitted `local.properties`. `NoOpBillingPort` |
| Sync | HTTP catalog delta + report flush exist; URLs empty unless set locally |
| CI | No GitHub Actions; quality gate is local `./scripts/check-quality.sh` + `:shared:jvmTest` |
| Result UI | Traffic-light header is the first `LazyColumn` item — it **scrolls away** |
| Dark theme | `CosmeticsTheme` follows the system; `DarkColors` only sets primary/onPrimary. `RatingColors` stay fixed |

The EU-allergens preset and the microplastics chip are correct on **fixture** unit tests. They barely apply to a real CosIng-scale scan until phase 16.

---

## Phase 16 — Maintained tags on the shipped catalog

**PR:** `cursor/phase-16-catalog-tags-4039`  
**Outcome:** Scanning CosIng `Geraniol` with the EU-allergens preset, or CosIng `Polyethylene`, behaves like the fixture tests — without a CosIng re-download and without replacing the 17k-product gzip.

### 16.1 Why not only re-pack

`CosingAssembler` already writes `ALLERGEN_26` / `ALLERGEN_80` / `MICROPLASTIC` at ingest time. The packed gzip was built **before** those indexes existed. A full `packShippedCatalog` needs `catalog/ingest/` (gitignored). Running it against `catalog/sources/` would **shrink** the shipped catalog back to eight fixtures.

Tag lists are code. Apply them when the in-memory index is assembled so:

- Already-installed `catalog.db` (same checksum) still gets the tags.
- JVM tests that go through `CatalogIndex.assemble` get the tags.
- The next real ingest pack still persists tags via `CosingAssembler`.

### 16.2 Behaviour

Add `MaintainedCatalogTags` (catalog pipeline) that **unions** existing `regulatoryTags` with:

- `EuLabelledAllergenIndex.tagsFor(inciName)`
- `MicroplasticIndex.tagsFor(inciName)`

Call it from `CatalogIndex.assemble` **before** wiring `EvaluateFormula` and `hazardsById`. Do not change `DangerLevel`. Do not invent hazard rows for ingredients that have none. Do not drop `ANNEX_*` or fixture editorial tags.

Do **not** rewrite SQLite in this PR (checksum / installer stay as they are). Do **not** commit a new `catalog.sqlite.gz`.

### 16.3 Done when

- Assemble a snapshot whose CosIng-like `POLYETHYLENE` row has empty tags → `Finding.microplastic()` is true; overall rating unchanged.
- Assemble a snapshot whose `GERANIOL` / `ACETYL CEDRENE` rows have empty tags → EU-allergens profile treats them as personal avoid (`ALLERGEN_26` / `ALLERGEN_80`).
- Existing annex tags on a name are kept.
- `:shared:jvmTest` and quality script green.

**Out of this PR:** CosIng re-ingest, gzip rewrite, `PHOTOTOXIC` / `CHILDREN` CosIng lists (still fixture editorial), vegan/cruelty chips.

---

## Phase 17 — Result header stays on screen

**PR:** `cursor/phase-17-sticky-result-header-4039`  
**Depends on:** 16 not required.  
**Outcome:** In landscape and at large font, the shopper still sees the rating while scrolling ingredients. The banner stays in the reserved bottom slot.

### 17.1 Behaviour

`ResultScreen` already uses `LazyColumn` + `Scaffold` `bottomBar` for ads. Pin the traffic-light header (overall word, shape, counts, microplastics chip, usage line) as a **sticky header**. The ingredient list, alternatives, and share actions scroll under it.

Keep TalkBack summary on the header. Do not put ads in the sticky region. Do not overlay the camera.

### 17.2 Done when

- Header remains visible when the first findings scroll off.
- Banner slot still reserved at the bottom.
- Quality script green; EN/PL unchanged unless a new content description is required.

**Out of this PR:** foldable two-pane camera/result; CJK layout.

---

## Phase 18 — Settings catalog notes

**PR:** `cursor/phase-18-catalog-notes-4039`  
**Depends on:** 16 (copy should match tags that actually apply).  
**Outcome:** Settings does not only show a version triple. It says what this ruleset includes, in shopper language.

### 18.1 Behaviour

Preferences catalog section: keep stamp (`version · built · region`). Add a short **notes** block from composeResources (EN/PL), not a fabricated “40 comments updated” count unless the packed meta actually stores one.

Cover, factually:

- EU labelled-allergen tags (26 and the 2023 expansion) drive the EU-allergens preset.
- Microplastics is an incomplete catalog tag, not a safety score.
- Hazard levels still come from the annex snapshot in this catalog.

Optional: one line that packed gzip may predate tag indexes and that the app applies current tag lists when loading.

### 18.2 Done when

- EN/PL keys for the notes.
- Stamp behaviour unchanged.
- Quality script green.

**Out of this PR:** a full ruleset changelog table in SQLite; live CosIng “what changed” feed.

---

## Phase 19 — CI quality gate

**PR:** `cursor/phase-19-ci-quality-4039`  
**Depends on:** none.  
**Outcome:** A pull request cannot merge on a red quality script or red `:shared:jvmTest` just because nobody ran them locally.

### 19.1 Behaviour

GitHub Actions on `pull_request` and `main`:

- JDK 21
- `./scripts/check-quality.sh`
- `./gradlew :shared:jvmTest`

Do **not** call live CosIng/OBF. Do **not** require Android SDK or `assembleDebug` in this PR (APK is optional and heavy). Cache the Gradle user home.

### 19.2 Done when

- Workflow file is in the repo.
- A failing EN/PL key or a failing jvmTest would fail the job.
- Quality script green on the PR itself.

**Out of this PR:** Play upload, iOS CI, instrumented camera tests.

---

## Phase 20 — Complete dark ColorScheme

**PR:** `cursor/phase-20-dark-scheme-4039`  
**Depends on:** none.  
**Outcome:** Dark mode is a real surface palette. Traffic lights do not change meaning.

### 20.1 Behaviour

Fill out `DarkColors` (background, surface, onSurface, error, …) so lists and cards are readable. **Do not** retint `RatingColors`. Do not let Material You recolour rating semantics (already the rule).

### 20.2 Done when

- Dark scheme sets the same roles as `LightColors`.
- `RatingColors.of` tests (or screenshot notes) still use the fixed greens/reds.
- Quality script green.

**Out of this PR:** a separate in-app theme override; dynamic color.

---

## Explicitly out of this slice

Same as earlier plans, plus items that need store/ops secrets rather than code:

| Item | Why it waits |
| --- | --- |
| Production AdMob unit IDs | Uncommitted `local.properties` / XCConfig only |
| Hosted catalog / report-flush URLs | Same; empty means bundled catalog |
| Real Remove-ads IAP | Needs store billing; keep `NoOpBillingPort` until then |
| Vegan / cruelty / palm / endocrine as **safety scores** | Incomplete data; must never enter the traffic light |
| Animal-derived **chip** (incomplete, like microplastics) | P3; only after 16–20 if product still wants it |
| Foldables two-pane | Optional polish, not shelf-blocking |
| CJK OCR models | Different market |
| Phototoxicity / children CosIng-wide lists | No maintained index yet; fixtures only |
| Accounts, cloud OCR, retailer scrape, medical modes | Product principles |

---

## Implementation order (one PR each)

```
16 maintained catalog tags   ← correctness; do this first
17 sticky result header
18 settings catalog notes    (after 16 so the copy is true)
19 CI quality gate           (independent; can parallel 17)
20 complete dark ColorScheme
```

---

## Testing (every phase)

- `./scripts/check-quality.sh` then `:shared:jvmTest`
- EN/PL key parity; files ≤ 500 lines; rethrow `CancellationException`; no `catch (Throwable)`
- Do not call live OBF/CosIng from unit tests
- Do not assemble or commit `releases/*.apk` unless explicitly asked
- Walk [quality-checklist.md](quality-checklist.md) points 1–11 before commit
