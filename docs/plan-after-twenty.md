# After phases 16–20: next work

Phases **16–20** in [plan-after-fifteen.md](plan-after-fifteen.md) are **shipped** on `main` (PRs **#25–#30**). This document is the next slice.

**Each phase is one PR.** Stack on `main` after the previous phase merges. Suggested branches: `cursor/phase-21-comment-preset-tags-4039` … `cursor/phase-23-animal-derived-chip-4039`. Do not combine two phases in one PR.

Related: [further-additions.md](further-additions.md), [plan.md](plan.md), [quality-checklist.md](quality-checklist.md).

**Do not mix extra scores into the hazard traffic light. Do not scrape retailers. Do not add accounts, cloud OCR, or medical modes.**

---

## Current baseline (after PR #30)

| Area | Today |
| --- | --- |
| Allergen / microplastic tags | Applied at ingest **and** `CatalogIndex.assemble` (`MaintainedCatalogTags`) |
| Phototoxic / children | Fixture `Salicylic Acid` only. Packed CosIng `SALICYLIC ACID` has `ANNEX_III,PREGNANCY_CAUTION` — **no** `PHOTOTOXIC` or `CHILDREN`. Packed `PHOTOTOXIC`/`CHILDREN` counts are **0**. |
| Pregnancy preset | Works on fixture Retinol / Salicylic Acid. CosIng `RETINAL` has empty tags. |
| Sun caution UI | Finding-row copy already exists (`finding_sun_caution`). It never fires on a CosIng-scale citrus oil. |
| Settings notes | Allergen, microplastics, annex, sun caution, children, and pregnancy: incomplete lists, not diagnoses, not the traffic light |
| Extra chips | Microplastics only. Animal-derived still P3 |

The children-caution preset and sun-caution badge are correct in **fixture** tests. They barely apply to a drugstore CosIng-scale scan until phase 21.

---

## Phase 21 — Phototoxic, children, and pregnancy tag indexes

**PR:** `cursor/phase-21-comment-preset-tags-4039`  
**Outcome:** CosIng `Salicylic Acid` with the children preset, CosIng `Retinal` with pregnancy caution, and CosIng bergamot peel oil show the same badges/presets as the fixtures — without a CosIng re-download.

### 21.1 Lists (incomplete, code-side)

Same pattern as `EuLabelledAllergenIndex` / `MicroplasticIndex`. Union into existing `regulatoryTags`. **Do not change `DangerLevel`.**

| Index | Tag | Names (minimum) | UI effect |
| --- | --- | --- | --- |
| `PhototoxicIndex` | `PHOTOTOXIC` | Salicylic Acid; expressed citrus peel oils: bergamot, lemon, lime, grapefruit, bitter orange | Existing finding **sun caution** line (not a diagnosis) |
| `ChildrenCautionIndex` | `CHILDREN` | Salicylic Acid, Methyl Salicylate | Children preset → personal avoid |
| `PregnancyCautionIndex` | `PREGNANCY_CAUTION` | Retinol, Retinal, Retinyl Palmitate, Retinyl Acetate, Salicylic Acid | Pregnancy preset → personal avoid |

Do **not** tag every AHA as phototoxic (sun sensitivity ≠ furocoumarin phototoxicity). Do **not** invent a long “not for under 3” annex dump.

Apply in `MaintainedCatalogTags.merge` and `CosingAssembler` (next ingest still persists tags).

### 21.2 Done when

- Assemble untagged CosIng-like `SALICYLIC ACID` → `sunCaution()` true; children preset marks personal avoid; overall rating unchanged.
- Assemble untagged `RETINAL` → pregnancy preset marks personal avoid; overall unchanged.
- Assemble untagged bergamot peel oil → `sunCaution()` true; overall unchanged.
- Glycerin stays untagged.
- `:shared:jvmTest` and quality script green.

**Out of this PR:** Settings copy; animal-derived chip; gzip rewrite.

---

## Phase 22 — Settings notes for the new tags

**PR:** `cursor/phase-22-preset-tag-notes-4039`  
**Depends on:** 21 (shipped as **#32**).  
**Outcome:** Catalog notes mention sun caution, children, and pregnancy tags in shopper language: incomplete lists, not diagnoses, not the traffic light.

Keep the existing allergen / microplastics / annex lines. EN/PL keys. Stamp unchanged.

### 22.1 Done when

- Settings catalog notes explain sun caution, children, and pregnancy tags.
- Copy says the lists are incomplete, not diagnoses, and not the traffic light.
- Existing allergen / microplastics / annex lines stay.
- Tag-load note covers the new lists. Stamp line is unchanged.

**Out of this PR:** a SQLite changelog table.

---

## Phase 23 — Animal-derived chip (optional, last)

**PR:** `cursor/phase-23-animal-derived-chip-4039`  
**Depends on:** 21 (tag-overlay pattern).  
**Outcome:** People who asked for vegan get a **separate** chip under the safety rating, same rules as microplastics.

- Incomplete INCI set (carmine, keratin, lactose, beeswax, lanolin, squalene, collagen, …). False negatives expected.
- Chip below the traffic light, not inside it. TalkBack: list is incomplete; not a safety rating.
- Overall rating unchanged.

**Out of this PR:** cruelty-free / Leaping Bunny (brand-level); palm; endocrine extra flag; treating the chip as SAFE/HIGH.

---

## Explicitly out of this slice

| Item | Why it waits |
| --- | --- |
| Production AdMob IDs, sync URLs | Uncommitted `local.properties` / XCConfig |
| Real Remove-ads IAP | Store billing; keep `NoOpBillingPort` |
| Vegan/cruelty/palm/endocrine as **safety scores** | Must never enter the traffic light |
| Foldables, CJK OCR | Optional / other markets |
| Accounts, cloud OCR, retailer scrape, medical modes | Product principles |

---

## Implementation order (one PR each)

```
21 phototoxic / children / pregnancy indexes   ← shipped #32
22 settings notes for those tags               ← this PR
23 animal-derived chip (optional last)
```

---

## Testing (every phase)

- `./scripts/check-quality.sh` then `:shared:jvmTest`
- EN/PL key parity; files ≤ 500 lines; rethrow `CancellationException`; no `catch (Throwable)`
- Do not call live OBF/CosIng from unit tests
- Rebuild `releases/inci-scan-debug.apk` when UI or catalog packing changes (phase 22 is UI copy)
- Walk [quality-checklist.md](quality-checklist.md) points 1–11 before commit
