using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.Json;
using System.Text.Json.Nodes;
using Json.Schema;

namespace AutoUncle.Avs;

/// <summary>One reason a document is invalid.</summary>
/// <param name="Pointer">JSON Pointer to the offending value, "" for the document itself.</param>
/// <param name="Message">What is wrong.</param>
public sealed record ValidationError(string Pointer, string Message)
{
    /// <inheritdoc />
    public override string ToString() => $"{(Pointer.Length == 0 ? "/" : Pointer)}: {Message}";
}

/// <summary>Thrown by <see cref="AvsSchema.ValidateOrThrow"/>; lists every validation error.</summary>
public sealed class InvalidAvsDocumentException : Exception
{
    /// <summary>Every validation error.</summary>
    public IReadOnlyList<ValidationError> Errors { get; }

    /// <summary>Creates the exception.</summary>
    public InvalidAvsDocumentException(IReadOnlyList<ValidationError> errors)
        : base("Invalid AVS document:\n" + string.Join("\n", errors.Select(e => "  " + e)))
    {
        Errors = errors;
    }
}

/// <summary>
/// AVS – AutoUncle Vehicle Schema.
/// <code>
/// var errors = AvsSchema.Validate(json);   // empty when valid
/// AvsSchema.IsValid(json);
/// AvsSchema.ValidateOrThrow(json);         // throws InvalidAvsDocumentException
/// </code>
/// </summary>
public static class AvsSchema
{
    /// <summary>The AVS JSON Schema as a string.</summary>
    public static string SchemaJson { get; } = ReadSchema();

    private static readonly JsonSchema Schema = JsonSchema.FromText(SchemaJson);

    private static readonly EvaluationOptions Options = new()
    {
        OutputFormat = OutputFormat.List,
        RequireFormatValidation = true,
    };

    /// <summary>The schema's own version, e.g. "0.2" (what documents put in <c>avs.version</c>).</summary>
    public static string SchemaVersion { get; } =
        JsonNode.Parse(SchemaJson)!["$defs"]!["Envelope"]!["properties"]!["version"]!["const"]!.GetValue<string>();

    /// <summary>Returns every validation error; an empty list means the document is valid.</summary>
    /// <exception cref="ArgumentException">The input is not JSON.</exception>
    public static IReadOnlyList<ValidationError> Validate(string json)
    {
        JsonNode? node;
        try
        {
            node = JsonNode.Parse(json);
        }
        catch (JsonException e)
        {
            throw new ArgumentException("Document is not JSON: " + e.Message, nameof(json), e);
        }
        return Validate(node);
    }

    /// <summary>Returns every validation error for an already parsed document.</summary>
    public static IReadOnlyList<ValidationError> Validate(JsonNode? document)
    {
        var results = Schema.Evaluate(document, Options);
        if (results.IsValid) return Array.Empty<ValidationError>();

        // A failed `if` only means its `then` does not apply; it is not an error in the document.
        return results.Details
            .Where(d => d.Errors is { Count: > 0 } && !IsConditionPath(d.EvaluationPath))
            .SelectMany(d => d.Errors!.Values.Select(m => new ValidationError(d.InstanceLocation.ToString(), m)))
            .Distinct()
            .OrderBy(e => e.Pointer, StringComparer.Ordinal)
            .ThenBy(e => e.Message, StringComparer.Ordinal)
            .ToList();
    }

    /// <summary>Whether the input is JSON and a valid AVS document.</summary>
    public static bool IsValid(string json)
    {
        try
        {
            return Validate(json).Count == 0;
        }
        catch (ArgumentException)
        {
            return false;
        }
    }

    /// <summary>Throws <see cref="InvalidAvsDocumentException"/> listing every error.</summary>
    public static void ValidateOrThrow(string json)
    {
        var errors = Validate(json);
        if (errors.Count > 0) throw new InvalidAvsDocumentException(errors);
    }

    private static bool IsConditionPath(Json.Pointer.JsonPointer path)
    {
        var segments = path.ToString().Split('/');
        return segments.Contains("if");
    }

    private static string ReadSchema()
    {
        using var stream = typeof(AvsSchema).Assembly.GetManifestResourceStream("AutoUncle.Avs.avs.schema.json")
            ?? throw new InvalidOperationException("avs.schema.json missing from the assembly");
        using var reader = new StreamReader(stream);
        return reader.ReadToEnd();
    }
}
