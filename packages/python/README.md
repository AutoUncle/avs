# autouncle-avs (Python)

The [AVS](https://github.com/AutoUncle/avs) JSON Schema, `TypedDict` types and a validator.

```bash
pip install "autouncle-avs @ git+https://github.com/AutoUncle/avs@v0.2.0#subdirectory=packages/python"
```

```python
import avs

avs.validate(doc)            # [] or [ValidationError(pointer="/vehicle/vin", message="...")]
avs.is_valid(doc)            # True / False
avs.validate_or_raise(doc)   # raises avs.InvalidDocument listing every error

from avs import AvsDocument  # TypedDict for type checkers
```

`doc` can be a JSON string, bytes or a parsed dict. The package version equals the schema release.
