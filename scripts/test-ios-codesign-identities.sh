#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/ios-codesign-identities.sh
. "$ROOT/scripts/lib/ios-codesign-identities.sh"

FIXTURES="$ROOT/scripts/testdata/ios-codesign"
FAILED=0

assert_field() {
  local selected="$1"
  local key="$2"
  local expected="$3"
  local actual
  actual="$(printf '%s\n' "$selected" | sed -n "s/^${key}=//p")"
  if [ "$actual" != "$expected" ]; then
    echo "FAIL: $key expected [$expected] got [$actual]"
    FAILED=1
  fi
}

assert_selects() {
  local fixture="$1"
  local team="$2"
  local source="$3"
  local selected
  if ! selected="$(select_ios_signing < "$FIXTURES/$fixture")"; then
    echo "FAIL: $fixture produced no signing selection"
    FAILED=1
    return
  fi
  assert_field "$selected" TEAM_ID "$team"
  assert_field "$selected" SOURCE "$source"
}

assert_no_selection() {
  local fixture="$1"
  if select_ios_signing < "$FIXTURES/$fixture" >/dev/null; then
    echo "FAIL: $fixture should not select a team"
    FAILED=1
  fi
}

assert_warning_present() {
  local fixture="$1"
  local selected
  selected="$(select_ios_signing < "$FIXTURES/$fixture")"
  if ! printf '%s\n' "$selected" | grep -q '^WARNING='; then
    echo "FAIL: $fixture should warn about mismatched teams"
    FAILED=1
  fi
}

assert_selects both.txt ABCD123456 apple_developer
assert_selects apple-dev-only.txt ABCD123456 apple_developer
assert_selects developer-id-only.txt ZYXW987654 developer_id
assert_selects installer-only.txt ZYXW987654 developer_id
assert_selects ignores-distribution.txt ZYXW987654 developer_id
assert_selects mismatch-teams.txt ABCD123456 apple_developer
assert_selects legacy-iphone-developer.txt ABCD123456 apple_developer
assert_warning_present mismatch-teams.txt
assert_no_selection none.txt

assert_sync_writes_local() {
  local out="$ROOT/iosApp/Configuration/Config.local.xcconfig"
  rm -f "$out"
  IOS_CODESIGN_IDENTITIES_FILE="$FIXTURES/both.txt" "$ROOT/scripts/sync-ios-signing.sh" >/dev/null
  if ! grep -q 'TEAM_ID = ABCD123456' "$out"; then
    echo "FAIL: sync-ios-signing.sh did not write TEAM_ID to Config.local.xcconfig"
    FAILED=1
  fi
  rm -f "$out"
}

assert_sync_writes_local

if [ "$FAILED" -ne 0 ]; then
  echo "iOS codesign identity parsing FAILED"
  exit 1
fi
echo "OK: iOS codesign identity parsing"
