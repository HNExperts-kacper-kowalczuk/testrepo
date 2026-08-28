# Cosmetic Ingredient Scanner

Kotlin Multiplatform (Android + iOS) app that lets people look up a cosmetic product and see whether its ingredients are potentially harmful, restricted, or otherwise worth avoiding.

The evaluator, catalog, and comments work **offline**. v1 can ship a CosIng-scale bundled catalog (see Phase 1 in `docs/plan-next-phases.md`). Scan a barcode or the printed INCI list on device, or type/paste if the camera cannot read the pack. If a barcode is missing from the offline catalog and the device is online, the printed INCI list is fetched from Open Beauty Facts (Open Food Facts as fallback) and scored immediately; that hit is cached on device.

UI copy and comments are structured for **easy translation** (English and Polish).

## Status (v1 in progress)

Working now:

- Live camera barcode scan (CameraX + ML Kit on Android, AVFoundation on iOS) with 800 ms debounce
- Still-image INCI OCR and a confirm-ingredients screen (edit / add / remove; fuzzy matches must be accepted or rejected)
- INCI matching (aliases, `1,2-Hexanediol`, fuzzy OCR typos, allergen appendix after Parfum)
- Hazard scoring + personal avoid-list (fragrance-free, pregnancy caution, EU allergens, children caution, alcohol in leave-on, essential-oil cluster)
- Usage confirm on the result screen when the catalog did not store leave-on vs rinse-off
- Shelf with formula-change badges, compare, local catalog alternatives, ingredient encyclopedia, history hazard insight
- Share a plain-text or image result, gallery barcode, copy or send unsent reports (hashes only)
- Microplastics chip and animal-derived chip off the traffic light (incomplete catalog tags, not safety scores)
- Wipe history, shelf, avoid-list, or all personal data on this device (catalog and disclaimer stay)
- SQLDelight catalog + user preferences/history, SHA-256 catalog checksum, last-updated stamp
- CosIng-derived + OBF-style catalog pipeline (`./scripts/build-catalog.sh`) and optional HTTP catalog delta / report flush when URLs are set in `local.properties`
- Compose Multiplatform UI: Scan, Camera, Confirm, Search, History, Preferences, Result
- Three-page first-launch onboarding + disclaimer; rating marks use shape + word + colour
- EN/PL string resources (key parity in CI) and localized ingredient comments
- AdMob banners after UMP (Android) / ATT (iOS) consent; slot collapses offline, on deny, on no-fill, or when release IDs are missing from `local.properties`; never on Scan, camera, OCR review, or Preferences
- Background work: catalog bootstrap, matching, OCR, and SQLite stay off the UI thread; catalog and user databases can run in parallel

The 21–26 slices are shipped. Next after this PR: accessibility / TalkBack work in [further-additions.md](docs/further-additions.md) §5.

Not in git / not as safety scores (see `docs/plan.md` and `docs/further-additions.md`):

- Production AdMob unit IDs belong in uncommitted `local.properties` (`admob.app.id`, `admob.banner.id`) / XCConfig; debug uses Google sample IDs
- Hosted catalog and report-flush URLs (`catalog.sync.url`, `reports.flush.url`) stay empty unless you set them locally
- Real Remove-ads IAP (`BillingPort` is a no-op until store billing exists)
- Vegan, cruelty-free, palm, or endocrine flags as extra safety scores

## Run

Android (JDK 21, Android SDK 36):

```bash
export ANDROID_HOME=/path/to/android-sdk
./gradlew :androidApp:assembleDebug
./gradlew :shared:jvmTest
```

Pull requests and `main` run `./scripts/check-quality.sh` and `:shared:jvmTest` (see `.github/workflows/quality.yml`). That job does not assemble the APK or call live CosIng/OBF.

iOS (macOS, Xcode, JDK 21):

```bash
./scripts/open-ios-xcode.sh
```

That reads **Apple Development** and **Developer ID** certificates from the **login** keychain, writes `iosApp/Configuration/Config.local.xcconfig` (`TEAM_ID` for Automatic signing), and opens `iosApp/iosApp.xcodeproj`. Pick a simulator or device and Run. The Xcode scheme and a build phase re-sync signing on each Run.

`Config.local.xcconfig` is gitignored. AdMob / catalog / report URLs still go in `iosApp/Configuration/Config.xcconfig`. Simulator Run works if the login keychain has no matching certs; a physical device needs an Apple Development identity in login.

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
| `5901234123532` | Bead Face Scrub | Safe (microplastics chip) |
| `5901234123549` | Carmine Lip Tint | Safe (animal-derived chip) |

Unknown barcodes are looked up online automatically when the device is connected. If no ingredient list is found, the Scan tab offers a fallback to photograph or paste the INCI list.

## Docs

- **[Product and architecture plan](docs/plan.md)**
- **[Next phases 1–5](docs/plan-next-phases.md)** — bundled catalog, pack verify, personal presets, compare, polish (shipped)
- **[Further improvements 6–15](docs/plan-further-improvements.md)** — trust settings through microplastics chip (shipped)
- **[After 1–15: phases 16–20](docs/plan-after-fifteen.md)** — CosIng tag overlay, sticky result header, catalog notes, CI, dark surfaces (shipped)
- **[After 16–20: phases 21–23](docs/plan-after-twenty.md)** — phototoxic/children/pregnancy tags, Settings notes, animal-derived chip (shipped)
- **[After 21–23: phases 24–26](docs/plan-after-twenty-three.md)** — ruleset changelog, store copy, in-app theme override (shipped)
- **[Internationalization](docs/i18n.md)**
- **[Further additions](docs/further-additions.md)**
- **[Module layout](docs/module-layout.md)**
- **[Quality checklist](docs/quality-checklist.md)** — 11 points; run `./scripts/check-quality.sh` before every commit
- **[User data backup](docs/user-data-backup.md)** — Auto Backup / iCloud of `user.db`
- **[Store listings and privacy labels](docs/store/play-en.md)**
- **[Catalog pipeline](catalog/README.md)**
