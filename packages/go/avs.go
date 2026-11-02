// Package avs bundles the AVS – AutoUncle Vehicle Schema and validates documents against it.
//
//	errs, err := avs.Validate(jsonBytes) // err only for malformed JSON
//	for _, e := range errs { fmt.Println(e) } // "/vehicle/vin: does not match pattern ..."
package avs

import (
	"bytes"
	_ "embed"
	"encoding/json"
	"fmt"
	"sort"
	"sync"

	"github.com/santhosh-tekuri/jsonschema/v6"
)

// SchemaJSON is the AVS JSON Schema.
//
//go:embed avs.schema.json
var SchemaJSON []byte

// ValidationError is one reason a document is invalid.
type ValidationError struct {
	// Pointer is a JSON Pointer to the offending value, "" for the document itself.
	Pointer string
	Message string
}

func (e ValidationError) String() string {
	p := e.Pointer
	if p == "" {
		p = "/"
	}
	return p + ": " + e.Message
}

var (
	compileOnce sync.Once
	compiled    *jsonschema.Schema
	version     string
)

func schema() *jsonschema.Schema {
	compileOnce.Do(func() {
		doc, err := jsonschema.UnmarshalJSON(bytes.NewReader(SchemaJSON))
		if err != nil {
			panic(fmt.Sprintf("avs: embedded schema is not JSON: %v", err))
		}
		c := jsonschema.NewCompiler()
		c.AssertFormat()
		if err := c.AddResource("avs.schema.json", doc); err != nil {
			panic(fmt.Sprintf("avs: %v", err))
		}
		compiled = c.MustCompile("avs.schema.json")

		var meta struct {
			Defs struct {
				Envelope struct {
					Properties struct {
						Version struct {
							Const string `json:"const"`
						} `json:"version"`
					} `json:"properties"`
				} `json:"Envelope"`
			} `json:"$defs"`
		}
		_ = json.Unmarshal(SchemaJSON, &meta)
		version = meta.Defs.Envelope.Properties.Version.Const
	})
	return compiled
}

// SchemaVersion is the schema's own version, e.g. "0.2" (what documents put in avs.version).
func SchemaVersion() string {
	schema()
	return version
}

// Validate checks a JSON document. It returns every validation error (none means valid),
// and a non-nil error only when the input is not JSON.
func Validate(document []byte) ([]ValidationError, error) {
	v, err := jsonschema.UnmarshalJSON(bytes.NewReader(document))
	if err != nil {
		return nil, fmt.Errorf("avs: document is not JSON: %w", err)
	}
	return ValidateValue(v), nil
}

// ValidateValue checks an already decoded document (as produced by encoding/json into any).
func ValidateValue(document any) []ValidationError {
	err := schema().Validate(document)
	if err == nil {
		return nil
	}
	verr, ok := err.(*jsonschema.ValidationError)
	if !ok {
		return []ValidationError{{Message: err.Error()}}
	}
	var out []ValidationError
	seen := map[ValidationError]bool{}
	for _, unit := range verr.BasicOutput().Errors {
		if unit.Error == nil {
			continue
		}
		e := ValidationError{Pointer: unit.InstanceLocation, Message: message(unit.Error)}
		if !seen[e] {
			seen[e] = true
			out = append(out, e)
		}
	}
	sort.SliceStable(out, func(i, j int) bool { return out[i].Pointer < out[j].Pointer })
	return out
}

// message renders an OutputError, which exposes its localized text only through MarshalJSON.
func message(e *jsonschema.OutputError) string {
	b, err := json.Marshal(e)
	if err != nil {
		return "invalid"
	}
	var s string
	if json.Unmarshal(b, &s) != nil {
		return string(b)
	}
	return s
}

// IsValid reports whether document is valid JSON and a valid AVS document.
func IsValid(document []byte) bool {
	errs, err := Validate(document)
	return err == nil && len(errs) == 0
}
