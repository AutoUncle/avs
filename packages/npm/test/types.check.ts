// Compile-time check, run by `npm run typecheck`: the generated types must keep the
// fields each identity level adds on top of CatalogueRef.
import type { AvsDocument } from "../src/index.js";

export const doc: AvsDocument = {
  avs: { version: "0.3", kind: "type" },
  identity: {
    brand: { name: "BMW", ids: [{ scheme: "autouncle", value: "b-example0001" }] },
    model: { name: "3 Series" },
    generation: { name: "G20/G21", code: "G20", fromYear: 2018, toYear: 2025 },
    phase: { name: "Launch", kind: "launch", index: 0 },
    body: { name: "Sedan", type: "sedan", doors: 4 },
    configuration: { name: "320e Sedan", seats: 5 },
  },
};
