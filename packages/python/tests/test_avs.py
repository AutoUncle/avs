import json
from pathlib import Path

import pytest

import avs

ROOT = Path(__file__).resolve().parents[3]
EXAMPLES = sorted((ROOT / "examples").glob("*.json"))
INVALID = sorted((ROOT / "tests" / "invalid").glob("*.json"))
VERSION = ".".join((ROOT / "VERSION").read_text().strip().split(".")[:2])


@pytest.mark.parametrize("path", EXAMPLES, ids=lambda p: p.name)
def test_examples_are_valid(path):
    assert avs.validate(path.read_text()) == []


@pytest.mark.parametrize("path", INVALID, ids=lambda p: p.name)
def test_invalid_fixtures_are_rejected(path):
    assert not avs.is_valid(path.read_text())


def test_fixture_dirs_are_not_empty():
    assert EXAMPLES and INVALID


def test_reports_pointer_for_bad_vin():
    doc = json.loads((ROOT / "examples" / "vehicle-bmw-320e-g20.json").read_text())
    doc["vehicle"]["vin"] = "NOT-A-VIN"
    assert [e.pointer for e in avs.validate(doc)] == ["/vehicle/vin"]


def test_validate_or_raise_lists_errors():
    with pytest.raises(avs.InvalidDocument) as info:
        avs.validate_or_raise({})
    assert info.value.errors


def test_schema_version_matches_version_file():
    assert avs.SCHEMA_VERSION == VERSION
