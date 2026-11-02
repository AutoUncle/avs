# Identifier schemes

AVS has no identifier of its own. Every catalogue record (brand, model, generation, phase, body, drivetrain, configuration, trim, package, option, feature) carries an `ids` list. Each entry gives the record's identifier in one code system:

```json
"brand": {
  "name": "BMW",
  "ids": [
    { "scheme": "autouncle", "value": "b-2d22e61bbd52b0b3327aefef" },
    { "scheme": "wikidata", "value": "Q26678" }
  ]
}
```

- **No scheme is privileged.** AutoUncle's catalogue (Axiom) is the `autouncle` scheme, the same as JATO's is `jato`.
- **Records match on a shared identifier.** Two references describe the same record when they share one identifier, meaning the same `scheme`, `value` and `country`. An omitted `country` is part of that key, not a wildcard. A consumer matches on whichever scheme it knows.
- **Only current identifiers are listed**, at most one per scheme and country. JSON Schema can't enforce that, so producers must.
- **An identifier belongs on the level it identifies.** A Wikidata item for "BMW 3 Series" goes on `model`. A KBA type key or TecDoc K-type identifies a registered type, body included, so it goes on `configuration`, never on the shared `drivetrain`.
- **Old identifiers keep resolving.** A merged or renamed record keeps answering to its old identifiers within its own scheme, so documents never need to list them.
- **`ids` is optional.** A record known only by name is still a valid reference.

## Registered schemes

Scheme names are lowercase and hyphenated (`^[a-z0-9]+(-[a-z0-9]+)*$`). To add a scheme, open a pull request that adds a row below and its rules to `$defs.Id` in the schema. The `country` column is enforced: a fixed country must be given with that value, "per market" must be given, and "not allowed" must be left out.

| Scheme | Code system | Typical levels | `country` |
|---|---|---|---|
| `autouncle` | AutoUncle catalogue (Axiom) | every level | not allowed |
| `wikidata` | Wikidata items (`Q…`, checked) | brand, model, generation | not allowed |
| `kba-hsn-tsn` | KBA Herstellerschlüssel/Typschlüssel, written `HSN/TSN` (e.g. `0005/BLH`, checked) | configuration | `DE` |
| `type-approval` | EU type-approval number (also see `identity.typeApproval`) | configuration | not allowed |
| `jato` | JATO vehicle id | configuration, trim | per market |
| `dat-europa-code` | DAT Europa-Code | configuration, trim | not allowed |
| `schwacke` | Schwacke code | configuration, trim | `DE` |
| `eurotax` | Eurotax code | configuration, trim | per market |
| `cap-hpi` | CAP code / CAP ID | configuration, trim | `GB` |
| `tecdoc-ktype` | TecDoc K-type | configuration | not allowed |
| `nhtsa-vpic` | NHTSA vPIC make/model ids | brand, model | `US` |
