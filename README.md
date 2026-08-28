# Cosmetic Ingredient Scanner

Kotlin Multiplatform (Android + iOS) app that lets people look up a cosmetic product and see whether its ingredients are potentially harmful, restricted, or otherwise worth avoiding.

The evaluator, catalog, and comments work **offline**. v1 can ship a CosIng-scale bundled catalog (see Phase 1 in `docs/plan-next-phases.md`). Scan a barcode or the printed INCI list on device, or type/paste if the camera cannot read the pack. If a barcode is missing from the offline catalog and the device is online, the printed INCI list is fetched from Open Beauty Facts (Open Food Facts as fallback) and scored immediately; that hit is cached on device.

UI copy and comments are structured for **easy translation** (English and Polish).

## Status (v1 in progress)

Working now:

- Live camera barcode scan (CameraX + ML Kit on Android, AVFoundation on iOS) with 800 ms debounce
- Still-image INCI OCR and a confirm-ingredients screen (edit / add / remove; fuzzy matches must be accepted or rejected)
- INCI matching (aliases, `1,2-Hexanediol`, fuzzy OCR typos)
- Hazard scoring + personal avoid-list (fragrance-free, pregnancy caution, EU allergens, children caution, alcohol in leave-on, essential-oil cluster)
- Shelf, compare, local catalog alternatives, ingredient encyclopedia
- Share a plain-text or image result, gallery barcode, unsent report copy
- Leave-on vs rinse-off scoring when the catalog or the user provides a product type
- SQLDelight catalog + user preferences/history, SHA-256 catalog checksum, last-updated stamp
- CosIng-derived + OBF-style catalog pipeline (`./scripts/build-catalog.sh`) and optional delta apply
- Compose Multiplatform UI: Scan, Camera, Confirm, Search, History, Preferences, Result
- Three-page first-launch onboarding + disclaimer; rating marks use shape + word + colour
- EN/PL string resources (key parity in CI) and localized ingredient comments
- AdMob banners after UMP (Android) / ATT (iOS) consent; slot collapses offline, on deny, on no-fill, or when release IDs are missing from `local.properties`; never on Scan, camera, OCR review, or Preferences
- Background work: catalog bootstrap, matching, OCR, and SQLite stay off the UI thread; catalog and user databases can run in parallel

Not in this slice yet (see `docs/plan.md` and `docs/further-additions.md`):

- Production AdMob unit IDs belong in uncommitted `local.properties` (`admob.app.id`, `admob.banner.id`) / XCConfig; debug uses Google sample IDs
- A full CosIng + Open Beauty Facts regional dump (pipeline + packer exist; Phase 1 bundles `catalog.sqlite.gz`)
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

Unknown barcodes are looked up online automatically when the device is connected. If no ingredient list is found, the Scan tab offers a fallback to photograph or paste the INCI list.

## Docs

- **[Product and architecture plan](docs/plan.md)**
- **[Next phases](docs/plan-next-phases.md)** — bundled catalog, pack verify, personal presets, compare, polish
- **[Internationalization](docs/i18n.md)**
- **[Further additions](docs/further-additions.md)**
- **[Module layout](docs/module-layout.md)**
- **[Quality checklist](docs/quality-checklist.md)** — 11 points; run `./scripts/check-quality.sh` before every commit
- **[User data backup](docs/user-data-backup.md)** — Auto Backup / iCloud of `user.db`
- **[Store listings and privacy labels](docs/store/play-en.md)**
- **[Catalog pipeline](catalog/README.md)**
