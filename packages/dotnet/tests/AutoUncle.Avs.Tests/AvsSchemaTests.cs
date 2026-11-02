using System;
using System.IO;
using System.Linq;
using System.Text.Json.Nodes;
using AutoUncle.Avs;
using Xunit;

public class AvsSchemaTests
{
    // Tests run from bin/<config>/net8.0; walk up to the repo root (the folder with VERSION).
    private static readonly string Root = FindRoot();

    private static string FindRoot()
    {
        var dir = new DirectoryInfo(AppContext.BaseDirectory);
        while (dir != null && !(File.Exists(Path.Combine(dir.FullName, "VERSION")) && Directory.Exists(Path.Combine(dir.FullName, "examples"))))
            dir = dir.Parent;
        return dir?.FullName ?? throw new InvalidOperationException("repo root not found");
    }

    private static string[] Json(string dir)
    {
        var files = Directory.GetFiles(Path.Combine(Root, dir), "*.json").OrderBy(f => f).ToArray();
        Assert.NotEmpty(files);
        return files;
    }

    [Fact]
    public void ExamplesAreValid()
    {
        foreach (var f in Json("examples"))
            Assert.True(AvsSchema.Validate(File.ReadAllText(f)).Count == 0, f + ": " + string.Join(", ", AvsSchema.Validate(File.ReadAllText(f))));
    }

    [Fact]
    public void InvalidFixturesAreRejected()
    {
        foreach (var f in Json(Path.Combine("tests", "invalid")))
            Assert.False(AvsSchema.IsValid(File.ReadAllText(f)), f + " should be rejected");
    }

    [Fact]
    public void ReportsPointerForBadVin()
    {
        var doc = JsonNode.Parse(File.ReadAllText(Path.Combine(Root, "examples", "vehicle-bmw-320e-g20.json")))!;
        doc["vehicle"]!["vin"] = "NOT-A-VIN";
        var errors = AvsSchema.Validate(doc);
        Assert.NotEmpty(errors);
        Assert.True(errors.All(e => e.Pointer == "/vehicle/vin"), string.Join("\n", errors));
    }

    [Fact]
    public void ValidateOrThrowListsErrors()
    {
        var e = Assert.Throws<InvalidAvsDocumentException>(() => AvsSchema.ValidateOrThrow("{}"));
        Assert.NotEmpty(e.Errors);
    }

    [Fact]
    public void MalformedJsonIsRejected()
    {
        Assert.Throws<ArgumentException>(() => AvsSchema.Validate("{"));
        Assert.False(AvsSchema.IsValid("{"));
    }

    [Fact]
    public void SchemaVersionMatchesVersionFile()
    {
        var v = File.ReadAllText(Path.Combine(Root, "VERSION")).Trim().Split('.');
        Assert.Equal(v[0] + "." + v[1], AvsSchema.SchemaVersion);
    }
}
