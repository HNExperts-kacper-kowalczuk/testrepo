# Parse `security find-identity -v -p codesigning` output.
# Selects TEAM_ID from Apple Development (preferred) or Developer ID certs.

identity_common_name() {
  printf '%s\n' "$1" | sed -n 's/.*"\(.*\)"$/\1/p'
}

identity_team_id() {
  printf '%s\n' "$1" | sed -n 's/.*(\([A-Z0-9]\{10\}\))$/\1/p'
}

identity_kind() {
  case "$1" in
    "Apple Development:"*|"Apple Developer:"*|"iPhone Developer:"*)
      printf '%s\n' apple_developer
      ;;
    "Developer ID Application:"*|"Developer ID Installer:"*)
      printf '%s\n' developer_id
      ;;
    *)
      printf '%s\n' other
      ;;
  esac
}

is_developer_id_application() {
  case "$1" in
    "Developer ID Application:"*) return 0 ;;
    *) return 1 ;;
  esac
}

_ios_sign_reset() {
  _IOS_APPLE_TEAM=
  _IOS_APPLE_NAME=
  _IOS_DEVID_TEAM=
  _IOS_DEVID_NAME=
}

_record_apple_developer() {
  local cn="$1"
  local team="$2"
  if [ -z "$_IOS_APPLE_TEAM" ]; then
    _IOS_APPLE_TEAM="$team"
    _IOS_APPLE_NAME="$cn"
  fi
}

_record_developer_id() {
  local cn="$1"
  local team="$2"
  if is_developer_id_application "$cn"; then
    _IOS_DEVID_TEAM="$team"
    _IOS_DEVID_NAME="$cn"
    return
  fi
  if [ -z "$_IOS_DEVID_TEAM" ]; then
    _IOS_DEVID_TEAM="$team"
    _IOS_DEVID_NAME="$cn"
  fi
}

_record_identity() {
  local kind="$1"
  local cn="$2"
  local team="$3"
  if [ "$kind" = apple_developer ]; then
    _record_apple_developer "$cn" "$team"
    return
  fi
  if [ "$kind" = developer_id ]; then
    _record_developer_id "$cn" "$team"
  fi
}

_emit_selected_signing() {
  if [ -n "$_IOS_APPLE_TEAM" ]; then
    printf 'TEAM_ID=%s\n' "$_IOS_APPLE_TEAM"
    printf 'SOURCE=apple_developer\n'
    printf 'IDENTITY=%s\n' "$_IOS_APPLE_NAME"
    if [ -n "$_IOS_DEVID_TEAM" ] && [ "$_IOS_DEVID_TEAM" != "$_IOS_APPLE_TEAM" ]; then
      printf 'WARNING=Developer ID team %s differs from Apple Development team %s; using Apple Development\n' \
        "$_IOS_DEVID_TEAM" "$_IOS_APPLE_TEAM"
    fi
    return 0
  fi
  if [ -n "$_IOS_DEVID_TEAM" ]; then
    printf 'TEAM_ID=%s\n' "$_IOS_DEVID_TEAM"
    printf 'SOURCE=developer_id\n'
    printf 'IDENTITY=%s\n' "$_IOS_DEVID_NAME"
    return 0
  fi
  return 1
}

# Reads find-identity -v lines on stdin.
# Prints TEAM_ID / SOURCE / IDENTITY (and optional WARNING) when a cert matches.
select_ios_signing() {
  local line cn kind team
  _ios_sign_reset
  while IFS= read -r line || [ -n "$line" ]; do
    cn="$(identity_common_name "$line")"
    [ -n "$cn" ] || continue
    kind="$(identity_kind "$cn")"
    team="$(identity_team_id "$cn")"
    [ -n "$team" ] || continue
    _record_identity "$kind" "$cn" "$team"
  done
  _emit_selected_signing
}

login_keychain_path() {
  local db="${HOME}/Library/Keychains/login.keychain-db"
  local legacy="${HOME}/Library/Keychains/login.keychain"
  if [ -e "$db" ]; then
    printf '%s\n' "$db"
    return 0
  fi
  if [ -e "$legacy" ]; then
    printf '%s\n' "$legacy"
    return 0
  fi
  return 1
}

dump_login_codesign_identities() {
  local keychain
  keychain="$(login_keychain_path)" || return 1
  security find-identity -v -p codesigning "$keychain"
}
