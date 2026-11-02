package com.autouncle.avs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.PathType;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

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
    private static final JsonSchema SCHEMA = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
            .getSchema(SCHEMA_NODE, SchemaValidatorsConfig.builder()
                    .formatAssertionsEnabled(true)
                    .pathType(PathType.JSON_POINTER)
                    .build());

    /** The schema's own version, e.g. "0.2" (what documents put in {@code avs.version}). */
    public static final String SCHEMA_VERSION =
            SCHEMA_NODE.at("/$defs/Envelope/properties/version/const").asText();

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
                .map(m -> new ValidationError(m.getInstanceLocation().toString(), stripLocation(m.getMessage())))
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

    // The validator prefixes each message with the instance location; we report that separately.
    private static String stripLocation(String message) {
        int colon = message.indexOf(": ");
        boolean prefixed = colon >= 0 && (message.startsWith("/") || message.startsWith("$") || colon == 0);
        return prefixed ? message.substring(colon + 2) : message;
    }

    private static JsonNode parse(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
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
