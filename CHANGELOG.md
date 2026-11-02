# Changelog

## 0.3.0 – draft

Breaking: catalogue identifiers are vendor-neutral.

- `id` on catalogue records is replaced by `ids`, a list of `{ scheme, value, country? }`. No scheme is privileged: AutoUncle's catalogue is the `autouncle` scheme. Two references describe the same record when they share an identifier.
- `identity.externalIds` is removed. Its identifiers now go on the level they identify (e.g. `wikidata` on `model`, `kba-hsn-tsn` on `drivetrain`).
- A trim's `drivetrains` are references (name and `ids`) instead of id strings.
- Scheme names must be lowercase and hyphenated. `docs/id-schemes.md` lists the registered schemes.

## 0.2.0 – draft

Breaking: some documents that were valid under 0.1 are now rejected.

- Unknown keys are rejected everywhere. Catalogue references and the levels that extend them (generation, phase, body, configuration, equipment, features) used to accept any key, so a typo such as `fromYr` validated.
- The document kind is enforced. A `type` document may not have a `vehicle` block, `fitted` flags, `fitted`/`absent` features or a battery passport id. A `vehicle` document must have a `vehicle` block and may not have `variations`, `drivetrains` or `standard`/`optional` features.
- `traction` gains `2wd`, for two-wheel drive where the driven axle is not known (registers often record only the number of driven axles).
- `vin` and `registration.plate` note that they can be personal data.
- `tests/invalid/` holds documents the schema must reject; CI checks each one.

## 0.1.0 – draft

- First draft. It has four layers: identity, specification, equipment and vehicle.
- The identity model is generation → phase → body × drivetrain → configuration.
- Equipment is described as one base trim plus packages and options, identified by name, with their contents as canonical features.
- Two examples: a car type and a physical vehicle.
