import { test } from "node:test";
import assert from "node:assert/strict";
import { readFileSync, readdirSync } from "node:fs";
import { createRequire } from "node:module";
import { validate, isAvsDocument, assertAvsDocument, InvalidAvsDocument, schemaVersion } from "../dist/index.js";

const examplesDir = new URL("../../../examples/", import.meta.url);
const examples = readdirSync(examplesDir).filter((f) => f.endsWith(".json"));
const load = (f) => JSON.parse(readFileSync(new URL(f, examplesDir), "utf8"));
const invalidDir = new URL("../../../tests/invalid/", import.meta.url);
const invalid = readdirSync(invalidDir).filter((f) => f.endsWith(".json"));
const VERSION = readFileSync(new URL("../../../VERSION", import.meta.url), "utf8").trim().split(".").slice(0, 2).join(".");

test("examples are valid", () => {
  assert.ok(examples.length > 0);
  for (const f of examples) assert.deepEqual(validate(load(f)), [], f);
});

test("invalid fixtures are rejected", () => {
  assert.ok(invalid.length > 0);
  for (const f of invalid) {
    const doc = JSON.parse(readFileSync(new URL(f, invalidDir), "utf8"));
    assert.notDeepEqual(validate(doc), [], `${f} should be rejected`);
  }
});

test("reports a pointer for a bad VIN", () => {
  const doc = load("vehicle-bmw-320e-g20.json");
  doc.vehicle.vin = "NOT-A-VIN";
  assert.deepEqual(validate(doc).map((e) => e.pointer), ["/vehicle/vin"]);
  assert.equal(isAvsDocument(doc), false);
});

test("assertAvsDocument throws with every error", () => {
  assert.throws(() => assertAvsDocument({}), (e) => e instanceof InvalidAvsDocument && e.errors.length > 0);
});

test("schema version matches VERSION", () => assert.equal(schemaVersion, VERSION));

test("CommonJS build works", () => {
  const require = createRequire(import.meta.url);
  const cjs = require("../dist/index.cjs");
  assert.deepEqual(cjs.validate(load("type-bmw-320e-g20-m-sport.json")), []);
});
