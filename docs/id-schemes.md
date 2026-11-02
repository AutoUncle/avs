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
- **Records match on a shared identifier.** Two references describe the same record when they share one identifier, meaning the same `scheme`, `value` and `country`. A consumer matches on whichever scheme it knows.
- **An identifier belongs on the level it identifies.** A Wikidata item for "BMW 3 Series" goes on `model`, and a KBA type key goes on `drivetrain` or `configuration`.
- **Old identifiers keep resolving.** A merged or renamed record keeps answering to its old identifiers within its own scheme.
- **`ids` is optional.** A record known only by name is still a valid reference.

## Registered schemes

Scheme names are lowercase and hyphenated (`^[a-z0-9]+(-[a-z0-9]+)*$`). To add a scheme, open a pull request that adds a row below.

| Scheme | Code system | Typical levels | `country` |
|---|---|---|---|
| `autouncle` | AutoUncle catalogue (Axiom) | every level | – |
| `wikidata` | Wikidata items (`Q…`) | brand, model, generation | – |
| `kba-hsn-tsn` | KBA Herstellerschlüssel/Typschlüssel, written `HSN/TSN` | drivetrain, configuration | `DE` |
| `type-approval` | EU type-approval number (also see `identity.typeApproval`) | configuration | – |
| `jato` | JATO vehicle id | configuration, trim | per market |
| `dat-europa-code` | DAT Europa-Code | configuration, trim | – |
| `schwacke` | Schwacke code | configuration, trim | `DE` |
| `eurotax` | Eurotax code | configuration, trim | per market |
| `cap-hpi` | CAP code / CAP ID | configuration, trim | `GB` |
| `tecdoc-ktype` | TecDoc K-type | drivetrain | – |
| `nhtsa-vpic` | NHTSA vPIC make/model ids | brand, model | `US` |
