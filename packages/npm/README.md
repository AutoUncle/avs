# @autouncle/avs (npm)

The [AVS](https://github.com/AutoUncle/avs) JSON Schema, generated TypeScript types and a validator.

```bash
echo "@autouncle:registry=https://npm.pkg.github.com" >> .npmrc
npm install @autouncle/avs
```

```ts
import { validate, isAvsDocument, assertAvsDocument, type AvsDocument } from "@autouncle/avs";

validate(doc);            // [] or [{ pointer: "/vehicle/vin", message: "must match pattern ..." }]
if (isAvsDocument(doc)) { doc.identity.brand.name; }   // narrowed to AvsDocument
assertAvsDocument(doc);   // throws InvalidAvsDocument listing every error
```

The raw schema is available as `import { schema } from "@autouncle/avs"` or `@autouncle/avs/schema.json`. Works with ESM and CommonJS.

The package version equals the schema release (`0.3.x` bundles schema `0.3`).
