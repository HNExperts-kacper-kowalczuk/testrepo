# Next phases: catalog, pack trust, profiles, compare, polish

This is the working plan for the rest of the product after scan, OCR crop, Fitatu-style UI, and automatic online GTIN lookup. Each phase leaves the app installable. Phases ship as sequential PRs on `cursor/next-phases-4039` (or follow-up branches stacked on it).

Related: [plan.md](plan.md), [plan-catalog-ocr-ui.md](plan-catalog-ocr-ui.md) (tracks A–D, largely done), [further-additions.md](further-additions.md), [catalog/README.md](../catalog/README.md).

**Do not mix extra scores into the hazard traffic light. Do not scrape retailers. Do not add accounts, cloud OCR, or medical modes.**

---

## Current baseline

| Area | Today |
| --- | --- |
| Catalog on device | 8 fixture products, ~36 ingredients, Kotlin seed |
| Ingest | CosIng ~36k substances + OBF ~17k GTIN+INCI products under `catalog/ingest/` (gitignored) |
| Packed DB | `CatalogSqlitePackager` writes fixture-only `catalog.sqlite.gz` |
| GTIN miss | If online, Open Beauty Facts / Open Food Facts → evaluate immediately; not persisted |
| OCR | Four-corner crop, ingredients-block extract, confirm editor (fuzzy accept/reject) |
| Profile | Pregnancy caution, fragrance-free, free-form avoid-list |
| Usage | Leave-on / rinse-off / lip / eye / spray on evaluate |
| User DB | History, profile, empty `user_shelf` and `report_queue` tables |
| Ads | Test AdMob IDs; banners never on Scan / camera / crop / confirm / Preferences |

---

## Phase 1 — Barcode hits become common

**Outcome:** Most EU/PL barcodes resolve offline. Online hits are remembered. OCR of an unknown pack matches CosIng-scale names, not 36 fixture rows.

### 1.1 Ship a bundled catalog

1. JVM task `packShippedCatalog`: merge **fixture products** (verified) + **OBF ingest** (skip GTINs already in fixtures) + **CosIng ingest** (fixture ingredient ids win on comment/hazard conflict).
2. `CatalogSqlitePackager.writeFromBuild(CatalogBuild)` — stop packing only the Kotlin fixtures.
3. Copy `catalog.sqlite.gz` + `catalog-manifest.json` into `composeResources/files/`.
4. Cap: gzip **≤ 20 MB** in git. If over: keep all CosIng substances, drop OBF products without PL/DE/store tags first, never drop annex II/III names.
5. `BundledCatalogInstaller` (android/ios): if `catalog.db` is missing or its checksum ≠ bundled manifest, gunzip the resource over the DB file **before** SQLDelight opens it. JVM tests stay in-memory + fixture seed.
6. `CatalogBootstrap`: seed fixtures only when the opened DB has no `catalog_meta`.
7. Settings catalog stamp shows the shipped version (not the fixture checksum). `BundledCatalogRemote` reads the bundled manifest.

Fixtures remain the matcher/evaluation unit-test corpus.

### 1.2 Cache online GTIN hits

Writable **user** DB (survives catalog replace):

```sql
CREATE TABLE cached_online_product (
    gtin TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    brand TEXT,
    inci_raw TEXT NOT NULL,
    usage TEXT NOT NULL,
    source TEXT NOT NULL,
    cached_at TEXT NOT NULL
);
```

`ResolveGtin`: catalog → cache → network. On `ReadyToEvaluate` with `source=online`, upsert cache. Next scan of that GTIN is offline.

Do not write community lists into `catalog.sqlite`.

### 1.3 Done when

- Fixture GTINs still evaluate the same.
- A real OBF PL GTIN in the packed DB resolves offline.
- Repeating an online-only GTIN does not hit HTTP.
- `:shared:jvmTest` and quality script green. Debug APK rebuilt.

---

## Phase 2 — Trust the formula on the pack

**Outcome:** Catalog/online lists can be checked against the printed INCI. Wrapping labels can be captured in more than one photo. Unknown barcodes are kept for later.

### 2.1 Check the label

Result (and optional Scan after a barcode hit): **Check the label** → existing crop → confirm → diff tokens vs stored `inci_raw`.

- Same set (order-insensitive, normalized): trust badge, keep catalog list.
- Mismatch: evaluate the **photographed** list; `source=ocr`; enqueue `report_queue.kind = wrong_inci` with both hashes. No network.

Needs `PendingVerifySession` (gtin, catalogInci, name, brand, usage) so crop/confirm know they are verifying, not a cold OCR.

### 2.2 Multi-shot OCR

Confirm screen: **Add another photo** → camera still → crop → merge token lists (append, drop exact normalized dupes). Caps at 3 shots.

### 2.3 Persist unknown GTINs

On `GtinResolution.Unknown`, insert `report_queue.kind = missing_product` (`gtin`, empty payload). After OCR evaluate with that GTIN, store `inci_raw` on the same row. Settings: count of unsent reports (flush UI in phase 5).

### 2.4 Done when

- Barcode result can re-score from a photo and keep or replace the list.
- Two photos of a wrap-around label merge on confirm.
- Unknown GTIN appears in `report_queue` without asking the user.

---

## Phase 3 — Stronger “suitable for me”

**Outcome:** Shoppers set common avoid rules without knowing INCI names. Hazard traffic light unchanged; personal avoid still drives `suitableForUser`.

### 3.1 Profile flags (user_profile columns)

| Flag | Behaviour |
| --- | --- |
| `eu_allergens` | Avoid CosIng/function tagged fragrance allergens (26, later 80) by ingredient id set in catalog |
| `children_caution` | Treat annex “not for children under 3” / `CHILDREN` tags as personal avoid; prefer stricter usage reading |
| `alcohol_leave_on` | Avoid `ALCOHOL DENAT.` and aliases only when usage is leave-on / lip / eye / spray |
| `essential_oil_cluster` | Avoid limonene, linalool, citral, geraniol, eugenol, and EO function tags |

Keep pregnancy + fragrance-free. Free-form avoid-list stays.

Preset membership is **data** (ingredient ids / tags in the catalog), not a hardcoded UI list of 80 names — except a small fixture override for tests.

### 3.2 INCI position (weak signal)

In result comments only: if a HIGH/RESTRICTED match is in the **first five** tokens, say so. Never hide PROHIBITED because it is last. No invented percentages.

### 3.3 Done when

- Toggling a preset changes `suitableForUser` on a fixture product in tests.
- Alcohol preset does not flag a rinse-off shampoo that only has Alcohol Denat. in a rinse-off context.
- EN/PL strings for every new switch.

---

## Phase 4 — Choose among products

**Outcome:** History is not the only memory. Shoppers can star, compare, and browse ingredients.

### 4.1 Shelf

Wire existing `user_shelf`. Result: star/unstar. New **Shelf** section on History (or Search): name, rating strip, date. Offline only.

### 4.2 Compare

Pick 2–3 history or shelf rows → `CompareScreen`: overall rating, unique HIGH/PROHIBITED names, shared personal-avoid hits. Re-evaluate from stored `inci_raw` (no network).

### 4.3 Local alternatives

On result, if `category` is set: up to 3 catalog products in the same category with a **better** overall (evaluate INCI offline, cap candidates at 20 by name). Label **In this app’s catalog**, never “in this shop”. Skip if category missing.

### 4.4 Ingredient encyclopedia

Search tab: segmented **Products | Ingredients**. Ingredient query uses `CatalogIndex.ingredientsSorted` + aliases; row opens a detail sheet (INCI, CAS, comments, danger). No scan required.

### 4.5 Done when

- Star survives catalog replace (user DB).
- Compare of two fixture history rows is deterministic in tests.
- Search finds “Glycerin” without a product query.

---

## Phase 5 — Ship-ready polish

**Outcome:** Store-ish UX without violating ads/privacy rules. Production AdMob IDs stay out until the user supplies them.

### 5.1 Share

Result: system share sheet, **plain text** (name, rating word, date, disclaimer). Optional later: simple PNG. No tracking URLs.

### 5.2 Gallery barcode

Scan tab / camera: pick image → existing barcode decoder on the bitmap. Then `ResolveGtin` as live scan.

### 5.3 Reports and backup

- Preferences: “Unsent reports: N” + copy-as-text (gtin + kind) until a real flush endpoint exists.
- Document Android Auto Backup / iOS iCloud of `user.db` in store privacy labels; enable backup flags if not already.
- TalkBack: keep rating word + counts; add shelf/compare labels.
- Home-screen shortcut: Android `SHORTCUT` to barcode camera (optional iOS).
- Ads: keep test IDs; add `ads_removed` in user_profile and collapse banners when true. **Remove ads IAP** is a stub (`BillingPort` no-op) until store accounts exist.

### 5.4 Done when

- Share produces the disclaimer line.
- A gallery photo of a fixture EAN evaluates.
- Banner slot stays collapsed when `ads_removed=1`.

---

## Explicitly out of scope (all phases)

Accounts, friends, cloud shelf, cloud OCR, retailer scrape, live prices, interstitials, vegan/cruelty mixed into the danger score, dermatological diagnosis, daily scan limits, paywalling comments.

---

## Implementation order

```
1.1 bundled catalog → 1.2 online cache
2.3 unknown GTIN queue → 2.1 check label → 2.2 multi-shot
3.1 presets → 3.2 position comment
4.4 encyclopedia (small) → 4.1 shelf → 4.2 compare → 4.3 alternatives
5.1 share → 5.2 gallery → 5.3 reports / backup / ads stub
```

Phase 1 is the gate: without CosIng-scale matching, verify-label and encyclopedia are thin.

## Testing (every phase)

- `:shared:jvmTest` and `./scripts/check-quality.sh`
- EN/PL key parity; files ≤ 500 lines; no `catch (Throwable)`
- Do not call live OBF/CosIng from unit tests
- Rebuild `releases/inci-scan-debug.apk` when UI or catalog packing changes
