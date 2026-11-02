#!/usr/bin/env bash
# Checks that the schema rejects every document in tests/invalid/. A fixture
# that is not valid JSON fails the run: check-jsonschema would reject it for
# the wrong reason, so it would test nothing. Exits 1 if any fixture is
# malformed or accepted.
set -u
cd "$(dirname "$0")/.."

if command -v check-jsonschema > /dev/null; then
  check=(check-jsonschema)
else
  check=(pipx run check-jsonschema)
fi

fail=0
for f in tests/invalid/*.json; do
  if ! python3 -m json.tool "$f" > /dev/null 2>&1; then
    echo "::error file=$f::not valid JSON, so it tests nothing"
    fail=1
  elif "${check[@]}" --schemafile schema/avs.schema.json "$f" > /dev/null 2>&1; then
    echo "::error file=$f::accepted, but the schema should reject it"
    fail=1
  fi
done
exit $fail
