# After phases 21–26: next work

Phases **21–26** in [plan-after-twenty.md](plan-after-twenty.md) and [plan-after-twenty-three.md](plan-after-twenty-three.md) are **shipped** (PRs **#32–#37**). OCR assist on confirm (auto-accept distance 1–2, catalog pick) is **#45–#46**. This document is the next slice.

**Each phase is one PR.** Stack on the current cosmetics tip after the previous phase merges. Suggested branches: `cursor/phase-27-typed-inci-confirm-4039` … `cursor/phase-29-gallery-inci-4039`. Do not combine two phases in one PR.

Related: [further-additions.md](further-additions.md), [plan.md](plan.md), [quality-checklist.md](quality-checklist.md).

**Do not mix extra scores into the hazard traffic light. Do not scrape retailers. Do not add accounts, cloud OCR, or medical modes.**

---

## Current baseline (after PR #46)

| Area | Today |
| --- | --- |
| OCR confirm | Unique distance 1–2 auto-fill; pending/unknown can pick from nearby + catalog search |
| Typed / pasted INCI | Scan “More ways” calls `EvaluateProduct` directly — no auto-accept, no suggestions, typos score as unknown |
| Result unknowns | Unmatched findings show a chip; the shopper cannot attach a catalog name without re-pasting |
| Gallery | Barcode camera can pick a photo; ingredient-list camera cannot |

---

## Phase 27 — Typed INCI uses the confirm review

**PR:** `cursor/phase-27-typed-inci-confirm-4039` (this PR)  
**Outcome:** Pasting or typing a list on Scan gets the same auto-accept and catalog picker as OCR. A list that is already all exact/alias matches still evaluates in one tap.

### 27.1 Behaviour

- `OpenTypedIngredientReview` runs `PrepareIngredientReview` off Main.
- If any token is pending, auto-filled, or unmatched: publish the draft and open Confirm. Keep `source=manual`. Carry the usage picked on Scan on the draft.
- If every token is exact or alias: evaluate immediately with the catalog names (skip Confirm).
- Scoring is unchanged. Ads stay off Confirm.

### 27.2 Done when

- `Aqua, Glycerin` from Scan still goes straight to Result.
- `Aqua, NIACINAM1DE` opens Confirm with Niacinamide auto-filled.
- `CompletelyUnknownStuff` opens Confirm with Pick an ingredient.
- `:shared:jvmTest` and quality script green.

**Out of this PR:** Result unmatched picker (28); gallery INCI (29).

---

## Phase 28 — Pick unmatched names on Result

**PR:** `cursor/phase-28-result-unmatched-pick-4039`  
**Depends on:** 27 (same picker + suggest use case).  
**Outcome:** An unknown finding can be attached to a catalog ingredient and the formula re-scored, without leaving Result.

### 28.1 Behaviour

- Unmatched finding rows offer **Pick an ingredient** (reuse the confirm picker sheet).
- Nearby + search use the unknown display name. Picking replaces that token in `inciRaw` and re-evaluates off Main.
- Keep GTIN / name / usage / source. Do not change the traffic-light rules.

### 28.2 Done when

- Replacing an unmatched token with Glycerin re-scores that name as exact.
- Matched findings do not show the picker.
- `:shared:jvmTest` and quality script green.

**Out of this PR:** gallery INCI (29).

---

## Phase 29 — Gallery photo of the ingredient list

**PR:** `cursor/phase-29-gallery-inci-4039`  
**Depends on:** 27 not required.  
**Outcome:** A shopper can pick a label photo (or a screenshot of a listing) and crop/OCR it the same way as a camera still. Works even when camera permission is denied.

### 29.1 Behaviour

- Ingredient-list camera: gallery control next to capture. Picked JPEG becomes a `CameraFrame` and opens crop.
- Do not run barcode decode on that photo. Empty/cancel failures are `AppFailure.Camera`.
- JVM stub stays a no-op. EN/PL keys.

### 29.2 Done when

- INCI mode shows a gallery control that publishes a still into `PendingCaptureSession`.
- Barcode gallery behaviour is unchanged.
- Quality script green; shared unit tests for frame hand-off where practical.

**Out of this PR:** CJK OCR models; cloud OCR.

---

## Explicitly out of this slice

| Item | Why it waits |
| --- | --- |
| Production AdMob IDs, sync URLs | Uncommitted `local.properties` / XCConfig |
| Real Remove-ads IAP | Store billing; keep `NoOpBillingPort` |
| Vegan/cruelty/palm/endocrine as **safety scores** | Must never enter the traffic light |
| Result Q&A / cloud LLM | Breaks offline-first |
| Foldables, CJK OCR | Optional / other markets |
| Accounts, retailer scrape, medical modes | Product principles |

---

## Implementation order (one PR each)

```
27 typed INCI uses confirm review   ← this PR
28 pick unmatched names on Result
29 gallery photo of the ingredient list
```

---

## Testing (every phase)

- `./scripts/check-quality.sh` then `:shared:jvmTest`
- EN/PL key parity; files ≤ 500 lines; rethrow `CancellationException`; no `catch (Throwable)`
- Do not call live OBF/CosIng from unit tests
- Do not assemble or commit `releases/*.apk` unless explicitly asked
- Walk [quality-checklist.md](quality-checklist.md) points 1–11 before commit
