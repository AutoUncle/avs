import Ajv2020 from "ajv/dist/2020.js";
import addFormats from "ajv-formats";
import schemaJson from "./avs.schema.json";
import type { AvsDocument } from "./types.js";

export type * from "./types.js";

/** The AVS JSON Schema. */
export const schema: Record<string, unknown> = schemaJson;

/** The schema's own version, e.g. "0.1" (what documents put in `avs.version`). */
export const schemaVersion: string = schemaJson.$defs.Envelope.properties.version.const;

export interface ValidationError {
  /** JSON Pointer to the offending value, "" for the document itself. */
  pointer: string;
  message: string;
}

export class InvalidAvsDocument extends Error {
  constructor(readonly errors: ValidationError[]) {
    super(`Invalid AVS document:\n${errors.map((e) => `  ${e.pointer || "/"}: ${e.message}`).join("\n")}`);
    this.name = "InvalidAvsDocument";
  }
}

const ajv = new Ajv2020({ allErrors: true, strict: false });
addFormats(ajv);
const check = ajv.compile<AvsDocument>(schemaJson);

/** Returns every validation error; an empty array means the document is valid. */
export function validate(document: unknown): ValidationError[] {
  if (check(document)) return [];
  return (check.errors ?? []).map((e) => ({ pointer: e.instancePath, message: e.message ?? e.keyword }));
}

export function isAvsDocument(document: unknown): document is AvsDocument {
  return check(document);
}

/** Throws InvalidAvsDocument listing every error. */
export function assertAvsDocument(document: unknown): asserts document is AvsDocument {
  const errors = validate(document);
  if (errors.length) throw new InvalidAvsDocument(errors);
}
