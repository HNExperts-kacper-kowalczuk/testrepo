# Cosmetic Ingredient Scanner

Kotlin Multiplatform (Android + iOS) app that lets people look up a cosmetic product and see whether its ingredients are potentially harmful, restricted, or otherwise worth avoiding.

The evaluator, catalog, and comments work **offline**. v1 ships a fixture catalog (8 products, ~35 ingredients). Type a barcode or paste an INCI list; on-device camera scanning is next.

UI copy and comments are structured for **easy translation** (English and Polish).

## Status (v1 in progress)

Working now:

- INCI matching (aliases, `1,2-Hexanediol`, fuzzy OCR typos)
- Hazard scoring + personal avoid-list (fragrance-free, pregnancy caution)
- SQLDelight catalog + user preferences/history
- Compose Multiplatform UI: Scan, Search, History, Preferences, Result
- EN/PL string resources and localized ingredient comments
- Reserved ad banner slot (hidden until network + consent; never on Scan)
- Background work: catalog bootstrap, matching, and SQLite stay off the UI thread; catalog and user databases can run in parallel

Not in this slice yet (see `docs/plan.md` and `docs/further-additions.md`):

- Live camera barcode / INCI OCR
- Real AdMob SDK
- Full regional product dump

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

Unknown barcodes fall through to the INCI paste field.

## Docs

- **[Product and architecture plan](docs/plan.md)**
- **[Internationalization](docs/i18n.md)**
- **[Further additions](docs/further-additions.md)**
- **[Module layout](docs/module-layout.md)**
- **[Data model](docs/data-model.md)**
