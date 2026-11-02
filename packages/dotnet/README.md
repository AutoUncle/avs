# AutoUncle.Avs (.NET)

The [AVS](https://github.com/AutoUncle/avs) JSON Schema and a validator. Targets .NET Standard 2.0 and .NET 8.

```bash
dotnet nuget add source https://nuget.pkg.github.com/AutoUncle/index.json --name autouncle
dotnet add package AutoUncle.Avs --version 0.2.0
```

```csharp
using AutoUncle.Avs;

var errors = AvsSchema.Validate(json);   // empty when valid; e.g. "/vehicle/vin: The string value is not a match for the indicated regular expression"
AvsSchema.IsValid(json);
AvsSchema.ValidateOrThrow(json);         // InvalidAvsDocumentException with every error
AvsSchema.SchemaJson;                    // the raw schema
```

The package version equals the schema release.
