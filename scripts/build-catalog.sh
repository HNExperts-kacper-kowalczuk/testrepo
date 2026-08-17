#!/usr/bin/env bash
set -euo pipefail

# Phase 6 catalog pipeline:
# 1. Encode the CosIng-derived ingredient table + OBF-like product dump
# 2. Validate HIGH/PROHIBITED comments in en+pl
# 3. Write catalog-manifest.json and catalog.sqlite.gz
# Swap catalog/sources/*.json for a larger regional dump without changing app code.

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

./gradlew :shared:exportCatalogSources
./gradlew :shared:jvmTest --tests com.hnexperts.cosmetics.catalog.pipeline.CatalogPipelineTest --tests com.hnexperts.cosmetics.catalog.application.ApplyCatalogDeltaTest --tests com.hnexperts.cosmetics.catalog.application.CheckCatalogUpdatesTest --tests com.hnexperts.cosmetics.catalog.domain.CatalogIntegrityTest --tests com.hnexperts.cosmetics.catalog.data.CatalogWriterTest

echo
echo "Wrote catalog/sources (CosIng-derived JSON + OBF JSON), catalog-manifest.json, and catalog/build/catalog.sqlite.gz."
echo "The mobile app still seeds the same snapshot on first launch; optional sync applies a delta when one is bundled."
