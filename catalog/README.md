# Catalog pipeline

CI (or `./scripts/build-catalog.sh`) ingests:

- `catalog/sources/cosing-ingredients.json` — CosIng-derived ingredient + hazard + comment rows
- `catalog/sources/obf-products.json` — Open Beauty Facts-style products with GTINs and printed INCI

The Kotlin pipeline (`CatalogBuilder`) validates that every `HIGH` / `PROHIBITED` ingredient has English and Polish comments, computes the SHA-256 catalog fingerprint, writes `catalog-manifest.json`, and packs `catalog/build/catalog.sqlite.gz`.

Replace those JSON files with a larger EU/PL dump without changing the mobile evaluation engine. Optional online sync only applies a delta when `CatalogDeltaSource` returns one; evaluation never waits on the network.

## CosIng / Open Beauty Facts ingest

`./gradlew :shared:ingestCatalogSources` fetches real source data and writes **candidate** dumps to `catalog/ingest/` (gitignored, plus `report.md` with counts):

- **CosIng inventory** (~36k substances/ingredients) via the public Europa Search API used by the CosIng web app, enumerated in token-prefix segments because the API caps any query at 10 000 results. Danger levels derive from the annexes: II → `PROHIBITED` (with templated EN/PL comments), III → `RESTRICTED`, IV/V/VI or known functions → `LOW`, otherwise `UNKNOWN`.
- **Open Beauty Facts** JSONL dump (cached under `catalog/ingest/cache/`), filtered to products with a valid GTIN and a usable INCI text, Poland first, then EU/EEA (~18k usable of ~73k products). Usage (rinse-off/spray/…) is guessed from category tags.

Pass extra flags with `-PingestArgs="--skip-obf --max-products=5000"`.

The app unpacks `shared/src/commonMain/composeResources/files/catalog.sqlite.gz` (~36k CosIng ingredients, ~17k OBF-style products). JVM tests keep using the curated fixtures.

`./gradlew :shared:packShippedCatalog` writes that gzip from **`catalog/ingest/`** when present (fixtures win on id/GTIN conflict), otherwise from `catalog/sources/` (fixture-sized). Do not run it without ingest dumps or you will replace the shipped catalog with eight SKUs.

Allergen and microplastic **tag indexes** are written at CosIng ingest (`CosingAssembler`) and applied when `CatalogIndex` is assembled (phase 16). Phototoxic, children, and pregnancy caution lists follow the same pattern in [plan-after-twenty.md](../docs/plan-after-twenty.md). Do not run `packShippedCatalog` without ingest dumps or you will replace the shipped catalog with eight SKUs.
