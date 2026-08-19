# Cosmetic Ingredient Scanner

Kotlin Multiplatform (Android + iOS) app that lets people look up a cosmetic product and see whether its ingredients are potentially harmful, restricted, or otherwise worth avoiding.

The evaluator, catalog, and comments work **offline**. v1 ships a fixture catalog (8 products, ~35 ingredients). Scan a barcode or the printed INCI list on device, or type/paste if the camera cannot read the pack.

UI copy and comments are structured for **easy translation** (English and Polish).

## Status (v1 in progress)

Working now:

- Live camera barcode scan (CameraX + ML Kit on Android, AVFoundation on iOS) with 800 ms debounce
- Still-image INCI OCR and a confirm-ingredients screen (edit / add / remove; fuzzy matches must be accepted or rejected)
- INCI matching (aliases, `1,2-Hexanediol`, fuzzy OCR typos)
- Hazard scoring + personal avoid-list (fragrance-free, pregnancy caution)
- Leave-on vs rinse-off scoring when the catalog or the user provides a product type
- SQLDelight catalog + user preferences/history, SHA-256 catalog checksum, last-updated stamp
- CosIng-derived + OBF-style catalog pipeline (`./scripts/build-catalog.sh`) and optional delta apply
- Compose Multiplatform UI: Scan, Camera, Confirm, Search, History, Preferences, Result
- Three-page first-launch onboarding + disclaimer; rating marks use shape + word + colour
- EN/PL string resources (key parity in CI) and localized ingredient comments
- AdMob test banners after UMP (Android) / ATT (iOS) consent; slot collapses offline, on deny, or on no-fill; never on Scan, camera, OCR review, or Preferences
- Background work: catalog bootstrap, matching, OCR, and SQLite stay off the UI thread; catalog and user databases can run in parallel

Not in this slice yet (see `docs/plan.md` and `docs/further-additions.md`):

- Production AdMob unit IDs (debug uses Google sample IDs)
- A full CosIng + Open Beauty Facts regional dump (pipeline ingests the same fixture SKUs; swap `catalog/sources/` for a larger dump)
- Live HTTP catalog hosting (the client applies a bundled delta when one is present)

## Run

Android (JDK 21, Android SDK 36):

```bash
export ANDROID_HOME=/path/to/android-sdk
./gradlew :androidApp:assembleDebug
./gradlew :shared:jvmTest
```

iOS: open `iosApp/iosApp.xcodeproj` on macOS after a Gradle sync.

### Fixture barcodes

| GTIN | Product | Typical rating |
| --- | --- | --- |
| `5901234123457` | Gentle Cream Cleanser | Low |
| `5901234123464` | Deep Clean Shampoo | High (MIT) |
| `5901234123471` | Daily Mineral Sunscreen | Moderate |
| `5901234123488` | Renew Night Cream | Restricted (retinol) |
| `5901234123495` | Citrus Body Mist | Moderate (fragrance) |
| `5901234123501` | Banned Actives Demo Paste | Prohibited |
| `5901234123518` | Niacinamide Serum 10% | Low |
| `5901234123525` | Plain Petrolatum Balm | Safe |

Unknown barcodes open a fallback to scan or paste the INCI list.

## Docs

- **[Product and architecture plan](docs/plan.md)**
- **[Next slice: catalog scale, OCR crop, Fitatu-style UI](docs/plan-catalog-ocr-ui.md)**
- **[Internationalization](docs/i18n.md)**
- **[Further additions](docs/further-additions.md)**
- **[Module layout](docs/module-layout.md)**
- **[Quality checklist](docs/quality-checklist.md)** — 11 points; run `./scripts/check-quality.sh` before every commit
- **[Store listings and privacy labels](docs/store/play-en.md)**
- **[Catalog pipeline](catalog/README.md)**
