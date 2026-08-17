#!/usr/bin/env bash
set -euo pipefail

# Catalog pipeline stand-in for Phase 6.
# Today the app seeds SQLite from FixtureCatalog and stamps catalog_meta with a SHA-256
# of versions + ingredient/product ids (see CatalogIntegrity).
# A later CI job should replace the fixture with CosIng-derived tables + an OBF GTIN dump,
# write catalog.sqlite.gz, and publish a manifest with the same checksum fields.

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

./gradlew :shared:jvmTest --tests com.hnexperts.cosmetics.catalog.domain.CatalogIntegrityTest --tests com.hnexperts.cosmetics.catalog.application.CheckCatalogUpdatesTest

echo
echo "Bundled catalog is the in-app fixture (EU, EN+PL comments)."
echo "No CosIng/OBF dump is ingested in this repository yet; evaluation stays offline."
