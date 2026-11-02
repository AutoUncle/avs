# Existing standards

AVS does not replace the standards below. Each covers part of what AVS covers. AVS reuses their conventions, and should be convertible to their formats.

| Standard | Covers | Open? | Relation to AVS |
|---|---|---|---|
| **schema.org `Car` / `Vehicle`** (auto extension) | Listings and SEO. Flat structure: trim is free text in `vehicleConfiguration` | Yes (CC-BY-SA) | Export target. Property names are reused where they fit |
| **COVESA VSS** | Live signal tree from the car (`Vehicle.TraveledDistance`, `…TractionBattery.StateOfHealth`) | Yes (MPL-2.0) | Odometer and battery fields are aligned with it |
| **EU Certificate of Conformity / type approval** (Reg. 2018/858, 2020/683) | Official EU type identity: approval number + Type/Variant/Version (TVV), numbered technical fields, WLTP | The legal text is public, the data is not | `identity.typeApproval`. Spec units follow the CoC |
| **eCoC via EUCARIS** | Signed per-VIN spec record for new cars from July 2026 | No (authorities and OEMs only) | The spec layer should map 1:1 |
| **EU Battery Passport** (Reg. 2023/1542, from Feb 2027) | Battery identity, capacity, state of health | The data model is open | `battery.passportId`, `history.batteryStateOfHealthPct` |
| **ISO 3779 / 3780** | VIN and manufacturer-code (WMI) structure | Paid | VIN pattern. EU VINs need not carry a check digit |
| **NHTSA vPIC** | US VIN decoder variables | Public domain | Vocabulary reference only |
| **National registers:** RDW (NL), DMR (DK), DVLA/MOT (UK), KBA HSN/TSN (DE) | Per-plate facts, inspections with odometer readings | Mixed | `ids` scheme `kba-hsn-tsn`; `vehicle.history.events` |
| **Listing feeds:** Google Vehicle Listings, Meta Automotive Inventory, mobile.de, AutoScout24 | What a listing must contain | The specs are public, the APIs are partner-only | Export targets |
| **Commercial catalogues:** JATO, Autovista (Eurotax/Schwacke), DAT, CAP HPI, Chrome Data, TecDoc | Rich make → model → version → equipment hierarchies | No, and their ids are incompatible | `ids` schemes (`jato`, `schwacke`, `dat-europa-code`, …) |
| **Catena-X / Tractus-X** (SAMM aspect models) | OEM/supplier data space, battery pass | Yes (CC-BY-4.0) | Inspiration for modular "aspects" |
| **MOBI Vehicle Identity, W3C Verifiable Credentials** | Vehicle identity and history as signed credentials | Partly | `history.events` is kept simple enough to wrap as credentials |
| **Wikidata** | Open brand and model items | Yes (CC0) | `ids` scheme `wikidata` |

## What none of them cover

1. **An open catalogue hierarchy below "model".** Generation → body → drivetrain → trim → feature exists only in closed, commercial systems.
2. **An open feature vocabulary with brand-specific names.** For example, BMW's "Active Cruise Control" is adaptive cruise control.
3. **A cross-reference between code systems.** These include KBA HSN/TSN, TVV, Schwacke, DAT, CAP and Wikidata.
4. **One car's facts gathered across countries.** National registers each expose only fragments.
