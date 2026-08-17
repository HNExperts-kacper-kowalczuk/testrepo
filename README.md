# Cosmetic Ingredient Scanner

Kotlin Multiplatform (Android + iOS) app that lets people scan a cosmetic product and see whether its ingredients are potentially harmful, restricted, or otherwise worth avoiding.

The app is designed to work **fully offline**: barcodes, products, ingredients, hazard levels, and explanatory comments ship with the device. If a product is missing from the catalog, the user can scan the ingredient list printed on the package (INCI / skład) and still get an evaluation.

This repository currently contains the product and technical plan, not the application source.

- **[Product and architecture plan](docs/plan.md)** — goals, user flows, domain model, offline database, scanning, evaluation, ads, and implementation phases
- **[Module layout](docs/module-layout.md)** — proposed Gradle / source-set structure
- **[Data model](docs/data-model.md)** — entities, matching rules, and the bundled SQLite schema
