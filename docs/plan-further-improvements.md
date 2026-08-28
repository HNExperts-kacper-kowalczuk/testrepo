# Further improvements: phases 6–15

**Status: shipped** (PRs #14–#23). Phases **1–5** in [plan-next-phases.md](plan-next-phases.md) were already shipped. This document is the completed next slice (trust settings through the microplastics chip).

**Each phase is one PR.** Stack on `main` after the previous phase merges. Suggested branches: `cursor/phase-6-trust-settings-4039` … `cursor/phase-15-microplastics-chip-4039`. Do not combine two phases in one PR. If a phase grows past a focused review, split it *before* opening the PR, and update this file.

Related: [further-additions.md](further-additions.md), [plan.md](plan.md), [quality-checklist.md](quality-checklist.md), [user-data-backup.md](user-data-backup.md).

**Do not mix extra scores into the hazard traffic light. Do not scrape retailers. Do not add accounts, cloud OCR, or medical modes.**

---

## Current baseline (after phases 1–5)

| Area | Today |
| --- | --- |
| Catalog | Bundled CosIng + OBF `catalog.sqlite.gz`; fixtures still the unit-test corpus |
| GTIN miss | OBF/OFF online → evaluate; hit cached in `cached_online_product` |
| Pack trust | Check the label, multi-shot OCR, `report_queue` for missing/wrong INCI |
| Profile | Pregnancy, fragrance-free, EU allergens, children, alcohol leave-on, EO cluster, free-form avoid |
| Choose | Shelf, compare 2–3, local alternatives (needs `category`), ingredient search |
| Polish | Plain-text share, gallery barcode, report copy-as-text, Android scan shortcut, `BillingPort` no-op |
| Hash | `product.inci_hash` is `inciRaw.hashCode().toString()` — **not** a stable identity |
| Ads | Google sample unit IDs; `ads_removed` collapses banners; Remove-ads button does nothing visible |
| Wipe | Preferences can clear **history** only |

---

## Phase 6 — Trust settings (honest ads, wipe data, TalkBack)

**PR:** `cursor/phase-6-trust-settings-4039`  
**Outcome:** Settings does not lie. Users can erase what this app stored. Screen readers can use Scan / Result / Shelf without guessing.

### 6.1 Honest Remove-ads

`NoOpBillingPort.purchaseRemoveAds()` returns `Ok(false)` and the Preferences button stays on screen.

- Add `BillingPort.isAvailable(): Boolean` (`false` on `NoOpBillingPort`).
- Preferences: show **Remove ads** only when `isAvailable()`. If you keep a placeholder, it must be a disabled label (`prefs_remove_ads_unavailable`), never a button that silently no-ops.
- Do **not** put production billing or live AdMob IDs in this PR.

### 6.2 Wipe user data

Add a `UserDataReset` application use case (user DB only; never `catalog.db`):

| Action | Clears |
| --- | --- |
| Clear history | existing `clearHistory` |
| Clear shelf | all `user_shelf` rows |
| Reset avoid-list | `user_avoid_ingredient` + preset flags stay unless “reset this device” |
| Reset this device | history, shelf, avoid-list, `report_queue`, `cached_online_product`, `ads_removed=0`, legal disclaimer **stays** (do not force onboarding again) |

SQLDelight: `clearShelf`, `clearReports`, `clearCachedProducts`. Confirm copy in EN/PL. Failures are `AppFailure` + Retry.

### 6.3 TalkBack / VoiceOver

- Tab icons: real `contentDescription` (`tab_scan`, …), not `null`.
- Result header: one summary string — overall word, prohibited/high counts, unknown count (reuse existing rating + unknown strings).
- Gallery, shelf star, compare checkboxes, share: named controls.

### 6.4 Done when

- With `NoOpBillingPort`, Preferences has no working Remove-ads button.
- Wiping shelf leaves history; “reset this device” leaves the catalog and the disclaimer flag.
- `:shared:jvmTest` covers reset; quality script green; EN/PL keys added.

**Out of this PR:** real IAP, iCloud entitlement, production AdMob IDs.

---

## Phase 7 — Confirm usage after a barcode / online hit

**PR:** `cursor/phase-7-usage-confirm-4039`  
**Depends on:** 6 not required (can stack on `main`).  
**Outcome:** Alcohol preset and Annex-style comments use the way the shopper uses the product, not a silent leave-on guess.

### 7.1 Behaviour

- Catalog/online `ProductUsage` other than `UNKNOWN`: keep current scoring; show “Scored as …” as today.
- `UNKNOWN` / `usageAssumed`: Result shows `UsagePicker` (already used on Confirm and manual paste). Choosing a usage **re-evaluates** on `computation`, writes history/shelf with the new usage, keeps GTIN/name/category.
- Do not block the camera. Show the assumed result first, picker beneath the header.
- Persist the chosen usage so reopen from History/Shelf does not ask again.

### 7.2 Done when

- Fixture rinse-off shampoo with only Alcohol Denat. + alcohol preset: assumed leave-on is not suitable; after picking rinse-off it is suitable.
- EN/PL for any new helper line (“Usage was assumed — pick how you use this”).
- No change to overall traffic-light rules — only `usage` input.

**Out of this PR:** inferring usage from category name; changing CosIng ingest.

---

## Phase 8 — Stable INCI hash and shelf formula watch

**PR:** `cursor/phase-8-formula-watch-4039`  
**Depends on:** 5 (shelf exists). Phase 7 optional.  
**Outcome:** Starred products can show “the catalog formula changed” after a catalog replace. Identity is a real hash, not `String.hashCode()`.

### 8.1 Stable hash

Add `InciIdentity.hash(inciRaw: String): String` in catalog/evaluation domain:

1. Reuse existing INCI tokenization / normalization (same as matcher), join with `,`.
2. `Sha256.hex` of that UTF-8 string (expect/actual already exists).
3. `CatalogWriter` and the shipped packer use it. Fixture tests assert a known hex for `Aqua, Glycerin`.

Repack is **not** required for the PR to merge if tests cover the writer; rebuild `catalog.sqlite.gz` only if you change packed rows. Prefer a writer unit test over a 7 MB git blob in this PR.

### 8.2 Store hash on the user shelf

`user_shelf.inci_hash TEXT` via `UserSchemaGuard`. Save on star; backfill empty with `InciIdentity.hash(inci_raw)`.

### 8.3 Watch after catalog load

After `catalog.awaitIndex()` on History (and optionally Result):

- For each shelf row with a GTIN or `product_id`, if the catalog product’s hash ≠ stored hash → badge “Catalog formula differs — check the label.”
- Tap uses existing Check-the-label / reopen + verify flow.
- Online-only cache rows: compare to `cached_online_product` if present; otherwise no badge.
- Never auto-restar or auto-rescore.

### 8.4 Done when

- Two tokenizations that differ only by spaces/case share a hash; adding Formaldehyde does not.
- A fixture shelf item whose stored hash is stale shows the badge in a VM/unit test with a fake catalog.
- Traffic light unchanged.

**Out of this PR:** push notifications, HTTP catalog fetch (phase 14).

---

## Phase 9 — History insight, export, compare usage

**PR:** `cursor/phase-9-history-insight-4039`  
**Depends on:** 5.  
**Outcome:** Shoppers can see patterns in *their* scans and copy their lists off-device without an account.

### 9.1 History insight

Use case `SummarizeScanHazards` on `computation`:

- Input: last N history rows (cap 100, already the history limit) + catalog index.
- Output: up to 5 ingredient display names that appear most often as `HIGH` or `PROHIBITED` in those evaluations (re-score stored INCI with current profile, or use stored findings if you persist them — **prefer re-score** so presets apply).
- History screen section: “In your recent scans” + names. Empty if fewer than 2 scans or no HIGH/PROHIBITED hits.
- Copy: educational, not “you are allergic.”

### 9.2 Export

Preferences, same pattern as report copy:

- Avoid-list: INCI names, one per line.
- Shelf: `name / gtin / rating / date`, one per line.
- Localized empty fallbacks. `copyPlainText` only.

### 9.3 Compare usage

`CompareScreen`: under each product, the usage word (`usage_leave_on`, …). History/shelf already store usage (after #12). No new scoring.

### 9.4 Done when

- Two fixture history INCI lists yield a deterministic top-name list in tests.
- Export strings exist in EN/PL.
- Compare shows rinse-off vs leave-on when those were stored.

**Out of this PR:** cloud backup of the export file; charts; medical wording.

---

## Phase 10 — Category for alternatives

**PR:** `cursor/phase-10-category-alternatives-4039`  
**Depends on:** 5 (alternatives already skip missing category).  
**Outcome:** “Lower-concern products in this category” can appear for OCR and unknown-usage scans, without pretending the shop stocks them.

### 10.1 Behaviour

- Result: if `category` is blank, show a compact category picker (existing catalog categories, cap ~20 by frequency, plus “Skip”).
- On pick: set `assessment.category`, persist on the current shelf/history rewrite if any, run existing `FindLocalAlternatives`.
- Optional suggestion: derive a *hint* from dominant function tags (cleanser vs moisturizer) — user must confirm. Wrong auto-category is worse than none.
- Label stays **In this app’s catalog**.

### 10.2 Done when

- OCR evaluate of a fixture INCI with no product row: picking `moisturizer` can surface the existing calm-moisturizer style fixture alternative in tests.
- Skip leaves alternatives empty.
- EN/PL for picker title.

**Out of this PR:** retailer availability; inventing new catalog categories in CosIng ingest.

---

## Phase 11 — Phototoxicity badge and allergen appendix

**PR:** `cursor/phase-11-comment-badges-4039`  
**Depends on:** 5.  
**Outcome:** Extra *information* on the result list, never a sixth traffic-light colour.

### 11.1 Phototoxicity / “not before sun”

- Catalog/fixture regulatory or comment tag e.g. `PHOTOTOXIC` on relevant EOs / AHAs already in fixtures where scientifically ordinary (bergamot-type, some AHAs) — **comments only**.
- Finding row: small text using a new string (`finding_sun_caution`), shown when the tag is present.
- Do not change `DangerLevel`. Do not say “causes burns” as a diagnosis.

### 11.2 Second INCI / allergens block

EU packs often list labelled allergens after `Parfum`. Confirm merge already appends photos; this phase:

- Matcher/normalizer: if a line starts with a known header (`Allergens`, `Alergeny`, `Contains:`) treat following comma-names as extra tokens of the **same** formula (no duplicate overall rating pass).
- Table tests: `Aqua, Parfum. Allergens: Limonene, Linalool` → three matched ids, not an unknown “Allergens: Limonene” token.

### 11.3 Done when

- Fixture with `PHOTOTOXIC` shows the badge; overall level equals baseline without the tag.
- Appendix test as above.
- EN/PL strings.

**Out of this PR:** expanding the 26→80 allergen *preset* (phase 12); SPF advice.

---

## Phase 12 — EU allergen preset from CosIng (toward 80)

**PR:** `cursor/phase-12-eu-allergens-ingest-4039`  
**Depends on:** 11 optional.  
**Outcome:** The EU-allergens switch is data-driven. No hardcoded list of 80 names in Compose.

### 12.1 Ingest

- Map CosIng / annex allergen labelling into `regulatoryTags` (`ALLERGEN_26` and/or `ALLERGEN_80`) in the catalog pipeline.
- Fixture override still wins for tests (limonene etc.).
- Preferences copy can say the list follows the labelled-allergen annex snapshot in this catalog version (Settings already shows catalog stamp).

### 12.2 Done when

- A CosIng-ingested allergen not in the tiny fixture set is `personalAvoid` when the preset is on (use a packed-test or pipeline unit test with a stub row — do not hit the network).
- Traffic light still ignores the preset (suitable-for-me only).
- Gzip cap in [plan-next-phases.md](plan-next-phases.md) still applies if you repack.

**Out of this PR:** UI checklist of 80 names; medical “fragrance allergy” mode.

---

## Phase 13 — Share image, iOS backup/shortcut, ads config

**PR:** `cursor/phase-13-store-polish-4039`  
**Depends on:** 6 (TalkBack strings help the share image).  
**Outcome:** Store-ish UX without live secrets in git.

Split into **two PRs** only if the Android/iOS actuals make review painful; default is one PR with three commits.

### 13.1 Share PNG

- `expect/actual` `shareResultImage` or extend `sharePlainText` with an optional bitmap path.
- Compose-free renderer on `computation`: product name, localized rating word, date, disclaimer. No QR, no URL.
- Result keeps text share; add “Share as image”.

### 13.2 iOS

- Home-screen shortcut / Spotlight equivalent if cheap; otherwise document “not on iOS yet” in `docs/user-data-backup.md` only if you still cannot add it.
- Enable iCloud **backup of the app container** (standard backup, not Documents) so `user.db` is included; update [user-data-backup.md](user-data-backup.md) and privacy labels.
- Gallery presenter: stop relying on deprecated `keyWindow` if a short `connectedScenes` helper exists in Kotlin/Native.

### 13.3 Ads config

- Debug: keep Google sample unit IDs.
- Release: read IDs from `local.properties` / XCConfig **not committed**. If missing, banners stay collapsed (`ads_removed` behaviour).
- Still no production IDs in the repo.

### 13.4 Done when

- JVM test: PNG or recorded text layout contains disclaimer (bitmap decode optional on JVM; at least the string payload is tested).
- Release build without IDs does not crash and does not show a test banner in production flavour.
- EN/PL for share-as-image.

**Out of this PR:** real Billing IAP; foldables; CJK OCR.

---

## Phase 14 — Hosted catalog delta and report flush

**PR:** `cursor/phase-14-catalog-sync-4039`  
**Depends on:** 8 (hash watch becomes useful when the catalog actually updates).  
**Outcome:** Catalog and reports can move without an app store release. Still no account.

### 14.1 Hosted catalog

- `CatalogRemote` HTTP implementation: GET manifest JSON + gzip delta/full, checksum via existing SHA-256, apply with existing `ApplyCatalogDelta` / installer rules.
- URL from build config (not hardcoded retailer domains). Offline: keep bundled catalog; Settings already has offline copy.
- Cap download size; refuse if checksum mismatches (`AppFailure.CorruptCatalog`).
- Do not write community lists into `catalog.sqlite`; same as phase 1.

### 14.2 Report flush

- POST (or a single multipart) of open `report_queue` rows: `kind`, `gtin`, payload hashes. No PII, no photos.
- On 2xx: mark `flushed=1`. Failure: keep rows, show verbose `AppFailure.Network`.
- Preferences: **Send unsent reports** in addition to copy-as-text.
- If the endpoint is not configured (debug), disable the button like phase 6 billing.

### 14.3 Done when

- Fake HTTP tests: good checksum applies; bad checksum does not replace the DB.
- Fake HTTP tests: flush marks rows flushed; 500 leaves them open.
- No live OBF/CosIng in unit tests.

**Out of this PR:** user login; syncing shelf to a server.

---

## Phase 15 — Microplastics chip (optional, last)

**PR:** `cursor/phase-15-microplastics-chip-4039`  
**Depends on:** 11 (comment-badge pattern).  
**Outcome:** People who asked for “plastic” get a **separate** chip under the safety rating.

- Tag list in catalog (`MICROPLASTIC` / “liquid plastic” INCI set) maintained in ingest + fixture override.
- Result: chip “Microplastics tag in this catalog” — **not** a new `DangerLevel`.
- Incomplete disclaimer in the chip content description.
- Tests: a fixture with the tag shows the chip; overall rating unchanged.

**Out of this PR:** vegan, cruelty-free, palm, endocrine extra flags (still P3 / avoid as safety scores).

---

## Explicitly out of scope (all phases)

Same as [plan-next-phases.md](plan-next-phases.md): accounts, friends, cloud shelf, cloud OCR, retailer scrape, live prices, interstitials, vegan/cruelty mixed into SAFE/HIGH, dermatological diagnosis, daily scan limits, paywalling comments, comedogenicity-as-science.

---

## Implementation order (one PR each)

```
6 trust settings
7 usage confirm
8 stable hash + shelf watch
9 history insight / export / compare usage
10 category picker for alternatives
11 phototoxicity badge + allergen appendix
12 EU allergen ingest (26→80 tags)
13 share image / iOS backup / ads config
14 hosted catalog + report flush
15 microplastics chip
```

Phases 6 and 7 are independent; prefer **6 first** (store trust). Phase 8 before 14. Phase 15 last on purpose.

---

## Testing (every phase)

- `./scripts/check-quality.sh` then `:shared:jvmTest`
- EN/PL key parity; files ≤ 500 lines; rethrow `CancellationException`; no `catch (Throwable)`
- Do not call live OBF/CosIng from unit tests
- Rebuild `releases/inci-scan-debug.apk` when UI or catalog packing changes
- Walk [quality-checklist.md](quality-checklist.md) points 1–11 before commit
