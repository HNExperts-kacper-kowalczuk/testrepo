# Catalog pipeline

CI (or `./scripts/build-catalog.sh`) ingests:

- `catalog/sources/cosing-ingredients.json` — CosIng-derived ingredient + hazard + comment rows
- `catalog/sources/obf-products.json` — Open Beauty Facts-style products with GTINs and printed INCI

The Kotlin pipeline (`CatalogBuilder`) validates that every `HIGH` / `PROHIBITED` ingredient has English and Polish comments, computes the SHA-256 catalog fingerprint, writes `catalog-manifest.json`, and packs `catalog/build/catalog.sqlite.gz`.

Replace those JSON files with a larger EU/PL dump without changing the mobile evaluation engine. Optional online sync only applies a delta when `CatalogDeltaSource` returns one; evaluation never waits on the network.

The next catalog slice (full CosIng substances, Open Beauty Facts GTIN ingest for EU/Poland chains, matcher rules for slash-compounds and abbreviations) is specified in **[docs/plan-catalog-ocr-ui.md](../docs/plan-catalog-ocr-ui.md)**. Do not scrape retailer sites as the primary source.
