# AVS – AutoUncle Vehicle Schema

An open, versioned JSON Schema for describing a car: what it is, how it is built and what it is equipped with. For one physical car, it also covers what has happened to it.

> **Status: draft 0.1.** The structure may still change. Feedback is welcome in issues.

| Path | What |
|---|---|
| [`schema/avs.schema.json`](schema/avs.schema.json) | The schema (JSON Schema draft 2020-12). Every field has a description. |
| [`examples/`](examples) | A car type (BMW 320e G20 Sedan, M Sport) and one physical car of that type. Catalogue ids are placeholders. |
| [`docs/existing-standards.md`](docs/existing-standards.md) | How AVS relates to schema.org, EU type approval, the Battery Passport, COVESA VSS and others. |
| [`CHANGELOG.md`](CHANGELOG.md) | Version history. |

## A document

AVS has two kinds of document. A **type** document describes a car as built, for example "BMW 320e G20 Sedan, M Sport". A **vehicle** document describes one physical car of that type.

```
avs            version, kind (type | vehicle)
identity       WHAT car type      → brand / series / model / generation / phase / body / drivetrain / configuration
                                    + typeApproval (EU TVV) + externalIds
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
- **Absence must be stated.** `fitted: false` or `availability: absent` means the car does not have it. A missing entry means unknown.
- **Condition is recorded as facts, not grades.** A damage is described by its area, its kind and whether it was repaired.

## Conventions

- **Units are part of the field name** (`powerKw`, `torqueNm`, `co2GKm`, `grossKwh`). Values follow EU Certificate of Conformity and WLTP conventions.
- **Fuel is described by two fields:** `energy` (what the car burns) and `electrification` (none, mhev, fhev, phev, erev, bev, fcev).
- **Catalogue ids are stable and never reused.** A merged record redirects to the record it was merged into.
- **Enums are in English, snake_case.** Countries use ISO 3166-1 alpha-2, currencies ISO 4217, VINs ISO 3779.

## Validating

```bash
pipx run check-jsonschema --check-metaschema schema/avs.schema.json
pipx run check-jsonschema --schemafile schema/avs.schema.json examples/*.json
```

CI runs both on every push and pull request.

## Versioning

AVS follows [semantic versioning](https://semver.org). Releases are git tags (`v0.1.0`), and consumers should pin a tag.

- **Patch:** descriptions, examples and docs change. Every valid document stays valid.
- **Minor:** new optional fields or enum values are added. Every valid document stays valid.
- **Major:** something that used to validate may no longer validate.

While the version is `0.x`, minor releases may still break compatibility.

## Licence

The schema and docs are licensed under [Apache-2.0](LICENSE). AVS defines the format only and contains no catalogue data.
