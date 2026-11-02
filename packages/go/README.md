# AVS for Go

The [AVS](https://github.com/AutoUncle/avs) JSON Schema (embedded) and a validator.

```bash
GOPRIVATE=github.com/AutoUncle go get github.com/AutoUncle/avs/packages/go@v0.3.0
```

```go
import avs "github.com/AutoUncle/avs/packages/go"

errs, err := avs.Validate(jsonBytes) // err only when the input is not JSON
for _, e := range errs {
	fmt.Println(e) // "/vehicle/vin: does not match pattern ..."
}
avs.IsValid(jsonBytes)
avs.SchemaJSON // the raw schema
```

Releases are tagged `packages/go/vX.Y.Z` (Go's convention for a module in a subdirectory), matching the schema release.
