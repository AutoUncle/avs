"""AVS – AutoUncle Vehicle Schema.

    avs.is_valid(doc)            # True / False
    avs.validate(doc)            # [] or [ValidationError(pointer="/vehicle/vin", message=...)]
    avs.validate_or_raise(doc)   # raises avs.InvalidDocument

A document may be a JSON string, bytes, or an already parsed dict.
"""

from __future__ import annotations

import json
from dataclasses import dataclass
from functools import lru_cache
from importlib.resources import files
from typing import Any, List, Union

from jsonschema import Draft202012Validator

from .types import AvsDocument

__all__ = [
    "AvsDocument",
    "InvalidDocument",
    "SCHEMA",
    "SCHEMA_VERSION",
    "ValidationError",
    "is_valid",
    "validate",
    "validate_or_raise",
]

SCHEMA: dict = json.loads(files(__package__).joinpath("avs.schema.json").read_text(encoding="utf-8"))

#: The schema's own version, e.g. "0.2" (what documents put in ``avs.version``).
SCHEMA_VERSION: str = SCHEMA["$defs"]["Envelope"]["properties"]["version"]["const"]

Document = Union[str, bytes, dict]


@dataclass(frozen=True)
class ValidationError:
    pointer: str
    """JSON Pointer to the offending value, "" for the document itself."""
    message: str

    def __str__(self) -> str:
        return f"{self.pointer or '/'}: {self.message}"


class InvalidDocument(ValueError):
    def __init__(self, errors: List[ValidationError]):
        self.errors = errors
        super().__init__("Invalid AVS document:\n" + "\n".join(f"  {e}" for e in errors))


@lru_cache(maxsize=1)
def _validator() -> Draft202012Validator:
    return Draft202012Validator(SCHEMA, format_checker=Draft202012Validator.FORMAT_CHECKER)


def _pointer(path) -> str:
    return "".join("/" + str(p).replace("~", "~0").replace("/", "~1") for p in path)


def validate(document: Document) -> List[ValidationError]:
    """Returns every validation error; an empty list means the document is valid."""
    data: Any = json.loads(document) if isinstance(document, (str, bytes)) else document
    errors = sorted(_validator().iter_errors(data), key=lambda e: list(map(str, e.absolute_path)))
    return [ValidationError(_pointer(e.absolute_path), e.message) for e in errors]


def is_valid(document: Document) -> bool:
    return not validate(document)


def validate_or_raise(document: Document) -> None:
    """Raises InvalidDocument listing every error."""
    errors = validate(document)
    if errors:
        raise InvalidDocument(errors)
