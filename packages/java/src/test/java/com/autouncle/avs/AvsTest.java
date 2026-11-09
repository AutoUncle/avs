package com.autouncle.avs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class AvsTest {
    private static final Path ROOT = Path.of("../..");

    private static List<Path> json(String dir) throws IOException {
        try (Stream<Path> s = Files.list(ROOT.resolve(dir))) {
            List<Path> files = s.filter(p -> p.toString().endsWith(".json")).sorted().toList();
            assertFalse(files.isEmpty(), dir + " is empty");
            return files;
        }
    }

    @Test
    void examplesAreValid() throws IOException {
        for (Path p : json("examples")) {
            assertEquals(List.of(), Avs.validate(Files.readString(p)), p.toString());
        }
    }

    @Test
    void invalidFixturesAreRejected() throws IOException {
        for (Path p : json("tests/invalid")) {
            assertFalse(Avs.isValid(Files.readString(p)), p + " should be rejected");
        }
    }

    @Test
    void reportsPointerForBadVin() throws IOException {
        ObjectNode doc = (ObjectNode) new ObjectMapper()
                .readTree(Files.readString(ROOT.resolve("examples/vehicle-bmw-320e-g20.json")));
        ((ObjectNode) doc.get("vehicle")).put("vin", "NOT-A-VIN");
        List<ValidationError> errors = Avs.validate(doc);
        assertFalse(errors.isEmpty());
        errors.forEach(e -> assertEquals("/vehicle/vin", e.pointer(), e.toString()));
        errors.forEach(e -> assertFalse(e.message().startsWith("/"), "message repeats the pointer: " + e.message()));
        System.out.println("sample error: " + errors.get(0));
    }

    @Test
    void validateOrThrowListsErrors() {
        InvalidAvsDocumentException e = assertThrows(InvalidAvsDocumentException.class, () -> Avs.validateOrThrow("{}"));
        assertFalse(e.errors().isEmpty());
    }

    @Test
    void malformedJsonIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Avs.validate("{"));
        assertFalse(Avs.isValid("{"));
    }

    @Test
    void schemaVersionMatchesVersionFile() throws IOException {
        String[] v = Files.readString(ROOT.resolve("VERSION")).trim().split("\\.");
        assertEquals(v[0] + "." + v[1], Avs.SCHEMA_VERSION);
        assertTrue(Avs.schemaJson().contains("\"$defs\""));
    }
}
