package com.autouncle.avs;

import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.path.PathType;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * AVS – AutoUncle Vehicle Schema.
 *
 * <pre>{@code
 * List<ValidationError> errors = Avs.validate(json);   // empty when valid
 * Avs.isValid(json);
 * Avs.validateOrThrow(json);                           // throws InvalidAvsDocumentException
 * }</pre>
 */
public final class Avs {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SCHEMA_JSON = readSchema();
    private static final JsonNode SCHEMA_NODE = parse(SCHEMA_JSON);
    private static final Schema SCHEMA = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                    builder -> builder.schemaRegistryConfig(SchemaRegistryConfig.builder()
                            .formatAssertionsEnabled(true)
                            .pathType(PathType.JSON_POINTER)
                            .build()))
            .getSchema(SCHEMA_NODE);

    /** The schema's own version, e.g. "0.2" (what documents put in {@code avs.version}). */
    public static final String SCHEMA_VERSION =
            SCHEMA_NODE.at("/$defs/Envelope/properties/version/const").asString();

    private Avs() {}

    /** The AVS JSON Schema as a string. */
    public static String schemaJson() {
        return SCHEMA_JSON;
    }

    /**
     * Returns every validation error; an empty list means the document is valid.
     *
     * @throws IllegalArgumentException if {@code json} is not JSON
     */
    public static List<ValidationError> validate(String json) {
        return validate(parse(json));
    }

    /** Returns every validation error for an already parsed document. */
    public static List<ValidationError> validate(JsonNode document) {
        return SCHEMA.validate(document).stream()
                .map(e -> new ValidationError(e.getInstanceLocation().toString(), e.getMessage()))
                .distinct()
                .sorted(Comparator.comparing(ValidationError::pointer).thenComparing(ValidationError::message))
                .toList();
    }

    public static boolean isValid(String json) {
        try {
            return validate(json).isEmpty();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Throws {@link InvalidAvsDocumentException} listing every error. */
    public static void validateOrThrow(String json) {
        List<ValidationError> errors = validate(json);
        if (!errors.isEmpty()) {
            throw new InvalidAvsDocumentException(errors);
        }
    }

    private static JsonNode parse(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Document is not JSON: " + e.getOriginalMessage(), e);
        }
    }

    private static String readSchema() {
        try (InputStream in = Avs.class.getResourceAsStream("avs.schema.json")) {
            if (in == null) {
                throw new IllegalStateException("avs.schema.json missing from the jar");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
