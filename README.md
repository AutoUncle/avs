# AVS – AutoUncle Vehicle Schema

An open, versioned JSON Schema for describing a car: what it is, how it is built and what it is equipped with. For one physical car, it also covers what has happened to it.

> **Status: draft 0.3.** The structure may still change. Feedback is welcome in issues.

| Path | What |
|---|---|
| [`schema/avs.schema.json`](schema/avs.schema.json) | The schema (JSON Schema draft 2020-12). Every field has a description. |
| [`examples/`](examples) | A car type (BMW 320e G20 Sedan, M Sport) and one physical car of that type. `autouncle` ids are placeholders. |
| [`tests/invalid/`](tests/invalid) | Small documents the schema must reject, one rule each. |
| [`docs/id-schemes.md`](docs/id-schemes.md) | How catalogue records are identified, and the registered code systems. |
| [`docs/existing-standards.md`](docs/existing-standards.md) | How AVS relates to schema.org, EU type approval, the Battery Passport, COVESA VSS and others. |
| [`CHANGELOG.md`](CHANGELOG.md) | Version history. |

## A document

AVS has two kinds of document. A **type** document describes a car as built, for example "BMW 320e G20 Sedan, M Sport". A **vehicle** document describes one physical car of that type.

```
avs            version, kind (type | vehicle)
identity       WHAT car type      → brand / series / model / generation / phase / body / drivetrain / configuration
                                    + typeApproval (EU TVV); every level carries ids in any code system
specification  HOW it's built     → body, powertrain, battery, charging, performance, efficiency[],
                                    dimensions, weights, wheels, safety, environmental labels, segment
equipment      WHAT it has        → trim (one) + packages + options, by name; features = contents
vehicle        WHICH physical car → VIN, registration, odometer, colour, interior, condition (damages),
                                    history (owners, inspections, events), warranty, price when new
extensions     vendor extras      → namespaced, e.g. "com.example"
```

## The identity model

```
Brand ─ (Series) ─ Model ─ Generation ─ Phase (launch / facelift)
                                │
                     ┌──────────┴──────────┐
                   Body                Drivetrain        ← independent; a drivetrain is shared by all bodies it was sold in
                     └──────────┬──────────┘
                          Configuration                  ← body × drivetrain as sold: seats, CO₂, consumption, range
                                │
                 Trim (one) + Packages + Options         ← by name; contents = features
```

## Principles

- **Facts only.** AVS says what the car is. How a fact was derived, where the car is for sale and what it is worth are out of scope. Those belong in companion schemas that reference an AVS document.
- **Each level is a separate fact.** Generation, phase, body, drivetrain and trim are independent of each other. Only brand and model are required. Anything unknown is left out, never guessed.
- **A drivetrain is a technical spec.** Two drivetrains differ only if the spec differs. Bodies, seats, trims and name tokens (OPF, ACT) never create a new drivetrain.
- **Anything that depends on both body and drivetrain lives on the configuration.** That covers seats, official consumption, CO₂ and electric range.
- **Facelifts are a phase, not a new generation.**
- **A trim is identified by name.** A car has exactly one base trim, and the trim name never contains a body word: "Carrera 4S Cabriolet" is trim *Carrera 4S* on body *Cabriolet*. What a trim contains is described by `features`.
- **A document is one kind.** A `type` document has no `vehicle` block, no `fitted` flags and only `standard`/`optional` features. A `vehicle` document has a `vehicle` block, no matching data (`variations`, `drivetrains`) and only `fitted`/`absent`/`unknown` features. The schema enforces this.
- **Absence must be stated.** `fitted: false` or `availability: absent` means the car does not have it. A missing entry means unknown.
- **Condition is recorded as facts, not grades.** A damage is described by its area, its kind and whether it was repaired.

## Conventions

- **Units are part of the field name** (`powerKw`, `torqueNm`, `co2GKm`, `grossKwh`). Values follow EU Certificate of Conformity and WLTP conventions.
- **Fuel is described by two fields:** `energy` (what the car burns) and `electrification` (none, mhev, fhev, phev, erev, bev, fcev).
- **AVS has no identifiers of its own.** Every catalogue record lists its `ids` in any code system (`autouncle`, `wikidata`, `kba-hsn-tsn`, `jato`, …), and none is privileged. Two references describe the same record when they share an identifier (same scheme, value and country). See [docs/id-schemes.md](docs/id-schemes.md).
- **Unknown keys are rejected** everywhere, including on catalogue references. Vendor data goes in `extensions`.
- **VINs and plates can be personal data.** Store and share them only where that is needed and allowed.
- **Enums are in English, snake_case.** Countries use ISO 3166-1 alpha-2, currencies ISO 4217, VINs ISO 3779.

## Validating

```bash
pipx run check-jsonschema --check-metaschema schema/avs.schema.json
pipx run check-jsonschema --schemafile schema/avs.schema.json examples/*.json
scripts/check-invalid.sh   # every tests/invalid/*.json must be rejected
```

CI runs all three on every push and pull request.

## Packages

Each package bundles the schema and a validator that returns every error with a JSON Pointer to it (`/vehicle/vin: …`). They are thin on purpose: they are generated or copied from `schema/`, never hand-written models. Each package's README has install and usage notes.

| Language | Package | Import | Types | Published to |
|---|---|---|---|---|
| Ruby | `autouncle-avs` | `AVS.validate(doc)` | – | GitHub Packages |
| TypeScript / JavaScript | `@autouncle/avs` | `validate(doc)` | `AvsDocument` (generated) | GitHub Packages |
| Python | `autouncle-avs` | `avs.validate(doc)` | `AvsDocument` TypedDict (generated) | Wheel on the GitHub Release |
| Go | `github.com/AutoUncle/avs/packages/go` | `avs.Validate(b)` | – | Tag `packages/go/vX.Y.Z` |
| Java / JVM | `com.autouncle:avs` | `Avs.validate(json)` | – | GitHub Packages |
| .NET | `AutoUncle.Avs` | `AvsSchema.Validate(json)` | – | GitHub Packages |
| PHP | `autouncle/avs` | `Avs::validate($doc)` | – | Composer, from the repository tag |

Every package's tests check the examples (must pass), `tests/invalid/` (must fail) and that its schema version matches `VERSION`. CI runs each language on every push and pull request.

**After changing `schema/` or `VERSION`, run `scripts/sync`.** It copies the schema and version into every package and regenerates the TypeScript and Python types. CI fails if a package is out of sync. You need node, uv and `npm ci` in `packages/npm`.

## Releasing

1. Set `VERSION`, update `CHANGELOG.md`, run `scripts/sync`, and merge.
2. Tag the merge commit `vX.Y.Z` (it must match `VERSION`) and push the tag. The release workflow publishes every package.

Dependabot opens weekly grouped updates for each package's dependencies and for GitHub Actions.

## Versioning

AVS follows [semantic versioning](https://semver.org). Releases are git tags (`v0.1.0`), and consumers should pin a tag. Every package's version equals the schema's.

- **Patch:** descriptions, examples and docs change. Every valid document stays valid.
- **Minor:** new optional fields or enum values are added. Every valid document stays valid.
- **Major:** something that used to validate may no longer validate.

While the version is `0.x`, minor releases may still break compatibility.

## Licence

The schema and docs are licensed under [Apache-2.0](LICENSE). AVS defines the format only and contains no catalogue data.
