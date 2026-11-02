package avs

import (
	"encoding/json"
	"os"
	"path/filepath"
	"strings"
	"testing"
)

func files(t *testing.T, pattern string) []string {
	t.Helper()
	paths, _ := filepath.Glob(filepath.Join("..", "..", pattern))
	if len(paths) == 0 {
		t.Fatalf("no files match %s", pattern)
	}
	return paths
}

func TestExamplesAreValid(t *testing.T) {
	for _, p := range files(t, "examples/*.json") {
		b, _ := os.ReadFile(p)
		errs, err := Validate(b)
		if err != nil || len(errs) > 0 {
			t.Errorf("%s: %v %v", p, err, errs)
		}
	}
}

func TestInvalidFixturesAreRejected(t *testing.T) {
	for _, p := range files(t, "tests/invalid/*.json") {
		b, _ := os.ReadFile(p)
		if IsValid(b) {
			t.Errorf("%s should be rejected", p)
		}
	}
}

func TestPointerForBadVIN(t *testing.T) {
	b, _ := os.ReadFile(filepath.Join("..", "..", "examples", "vehicle-bmw-320e-g20.json"))
	var doc map[string]any
	_ = json.Unmarshal(b, &doc)
	doc["vehicle"].(map[string]any)["vin"] = "NOT-A-VIN"
	errs := ValidateValue(doc)
	if len(errs) == 0 {
		t.Fatal("expected errors")
	}
	for _, e := range errs {
		if e.Pointer != "/vehicle/vin" {
			t.Errorf("unexpected pointer %q", e.Pointer)
		}
	}
}

func TestMalformedJSON(t *testing.T) {
	if _, err := Validate([]byte("{")); err == nil {
		t.Error("expected an error for malformed JSON")
	}
}

func TestSchemaVersionMatchesVersionFile(t *testing.T) {
	b, _ := os.ReadFile(filepath.Join("..", "..", "VERSION"))
	want := strings.Join(strings.SplitN(strings.TrimSpace(string(b)), ".", 3)[:2], ".")
	if got := SchemaVersion(); got != want {
		t.Errorf("SchemaVersion() = %q, want %q", got, want)
	}
}
