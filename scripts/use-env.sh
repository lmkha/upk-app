#!/usr/bin/env bash
# Copies the files of one environment from secrets/<name>/ to the places the
# build reads them from. secrets/ is git-ignored and handed out by the
# project owner.
#
# Usage:
#   scripts/use-env.sh <dev|staging|prod>
#
# Windows uses use-env.bat. Keep the two scripts in sync.

set -euo pipefail

cd "$(dirname "$0")/.."

# "<file in secrets/<name>/>:<destination in the repository>"
# Add a line here, and in use-env.bat, when the setup needs another file.
FILES=(
  "secrets.properties:secrets.properties"
  "google-services.json:app/google-services.json"
)

fail() {
  echo "error: $*" >&2
  exit 1
}

[ -d secrets ] ||
  fail "secrets/ not found. Ask the project owner for it and put it in the repository root."

environment="${1:-}"
case "${environment}" in
  "" | .* | */*) environment="" ;;
esac
source_dir="secrets/${environment}"
if [ -z "${environment}" ] || [ ! -d "${source_dir}" ]; then
  available="$(cd secrets && ls -d -- */ 2>/dev/null | tr -d '/' | tr '\n' ' ')"
  fail "usage: scripts/use-env.sh <environment>. Available in secrets/: ${available}"
fi

# Check everything before copying anything, so a failure never leaves the files
# of two environments mixed.
for entry in "${FILES[@]}"; do
  name="${entry%%:*}"
  [ -f "${source_dir}/${name}" ] || fail "${source_dir}/${name} not found."
done

# Remove the files of the previous environment before writing the new ones. If a
# copy then fails, a file is missing and the build stops, instead of running on
# the files of two environments.
for entry in "${FILES[@]}"; do
  rm -f "${entry#*:}"
done

for entry in "${FILES[@]}"; do
  cp "${source_dir}/${entry%%:*}" "${entry#*:}"
done

echo "Environment set to '${environment}'. Sync Gradle in Android Studio before the next build."
