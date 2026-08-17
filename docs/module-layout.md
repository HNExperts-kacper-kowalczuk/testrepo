# Module layout

Domain-oriented Gradle modules. Shared Kotlin lives in `commonMain`; Android/iOS adapters in `androidMain` / `iosMain`.

```
cosmetics-scanner/
├── settings.gradle.kts
├── gradle/
├── composeApp/                  # app shell: navigation, theme, DI graph
│   ├── src/commonMain/
│   ├── src/androidMain/         # Application, AdMob init, ML Kit barcode/OCR
│   └── src/iosMain/             # MainViewController, Vision, GADBannerView
├── iosApp/                      # Xcode host
├── core/
│   ├── domain/                  # shared kernel types used across contexts
│   ├── database/                # SQLDelight schema, drivers, bundled catalog copy
│   ├── camera/                  # BarcodeScanner + IngredientListRecognizer contracts
│   ├── i18n/                    # AppLocale, LocaleController, UiText, CommentLocalizer
│   └── common-ui/               # buttons, rating colors, disclaimer, spacing, composeResources XML
├── catalog/
│   ├── domain/
│   ├── application/
│   └── data/                    # product / GTIN repositories
├── ingredients/
│   ├── domain/                  # Ingredient, Alias, IngredientMatcher
│   ├── application/
│   └── data/
├── hazards/
│   ├── domain/                  # DangerLevel, HazardPolicy, comments
│   └── data/
├── evaluation/
│   ├── domain/                  # Formula, Finding, ProductAssessment
│   └── application/             # EvaluateFormula use case
├── scanning/
│   ├── application/             # ScanProduct, ScanIngredientList
│   └── ui/                      # camera screens, OCR review
├── preferences/
│   ├── domain/                  # UserAvoidanceProfile
│   └── data/
├── ads/
│   └── ui/                      # BannerAd expect/actual, AdPolicy, consent wrapper
└── sync/                        # optional; Ktor manifest + delta apply
    ├── application/
    └── data/
```

## Source-set rule

| Code | Location |
| --- | --- |
| Entities, value objects, matcher, scoring | `*/domain` `commonMain` |
| Use cases | `*/application` `commonMain` |
| SQLDelight, file copy, HTTP | `*/data` + `:core:database` |
| Compose screens, ViewModels | `composeApp` or `*/ui` `commonMain` |
| UI strings / plurals | `:core:common-ui` `composeResources/values[-xx]` (single `Res` module) |
| Locale + `UiText` | `:core:i18n` `commonMain` |
| Camera, OCR, AdMob, ATT | `androidMain` / `iosMain` only |

## Class size

Keep types focused:

- `IngredientMatcher` — tokenize + resolve names (no Compose, no SQL).
- `HazardPolicy` — map an ingredient + user profile to a `Finding`.
- `EvaluateFormula` — load ingredients, call matcher/policy, return `ProductAssessment`.
- ViewModels — one screen each; map `ProductAssessment` to `ResultUiState` using `UiText`, not raw strings.

If a class approaches 300–500 lines, split by responsibility (parsing vs scoring vs persistence), not by technical layer across domains.

## Dependency direction

```
composeApp → scanning / evaluation / catalog / ads / preferences
scanning   → evaluation, catalog, core:camera, core:i18n
evaluation → ingredients, hazards, preferences, core:domain
catalog    → ingredients, core:database
hazards    → core:i18n          # CommentLocalizer only; no XML
ads        → (nothing in domain)
sync       → core:database
core:common-ui → core:i18n
```

`ads` must not import `evaluation` or `catalog`. Evaluation must not import Compose or AdMob. Evaluation must not import `Res.string` — it returns codes; UI maps them.
