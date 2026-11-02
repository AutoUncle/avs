<?php

declare(strict_types=1);

namespace AutoUncle\Avs;

use Opis\JsonSchema\Errors\ErrorFormatter;
use Opis\JsonSchema\Validator;

/**
 * AVS – AutoUncle Vehicle Schema.
 *
 *     Avs::validate($json);          // [] or [ValidationError("/vehicle/vin", "...")]
 *     Avs::isValid($json);
 *     Avs::validateOrThrow($json);   // throws InvalidAvsDocument
 *
 * A document may be a JSON string, a decoded object, or an associative array.
 */
final class Avs
{
    /** The schema bundled with this package (the package is the whole AVS repository). */
    public const SCHEMA_PATH = __DIR__ . '/../../../schema/avs.schema.json';

    private const SCHEMA_ID = 'https://schema.autouncle.com/avs/avs.schema.json';

    private static ?Validator $validator = null;
    private static ?string $schemaJson = null;

    public static function schemaJson(): string
    {
        return self::$schemaJson ??= (string) file_get_contents(self::SCHEMA_PATH);
    }

    /** The schema's own version, e.g. "0.2" (what documents put in avs.version). */
    public static function schemaVersion(): string
    {
        return json_decode(self::schemaJson(), false, 512, JSON_THROW_ON_ERROR)
            ->{'$defs'}->Envelope->properties->version->const;
    }

    /**
     * Returns every validation error; an empty list means the document is valid.
     *
     * @return list<ValidationError>
     * @throws \InvalidArgumentException if a string document is not JSON
     */
    public static function validate(string|array|object $document): array
    {
        $result = self::validator()->validate(self::decode($document), self::SCHEMA_ID);
        if ($result->isValid()) {
            return [];
        }

        $errors = [];
        foreach ((new ErrorFormatter())->format($result->error(), true) as $pointer => $messages) {
            $pointer = $pointer === '/' || $pointer === '#' ? '' : ltrim((string) $pointer, '#');
            foreach ((array) $messages as $message) {
                $errors[] = new ValidationError($pointer, $message);
            }
        }
        usort($errors, fn ($a, $b) => [$a->pointer, $a->message] <=> [$b->pointer, $b->message]);

        return $errors;
    }

    public static function isValid(string|array|object $document): bool
    {
        try {
            return self::validate($document) === [];
        } catch (\InvalidArgumentException) {
            return false;
        }
    }

    /** @throws InvalidAvsDocument listing every error */
    public static function validateOrThrow(string|array|object $document): void
    {
        $errors = self::validate($document);
        if ($errors !== []) {
            throw new InvalidAvsDocument($errors);
        }
    }

    private static function validator(): Validator
    {
        if (self::$validator === null) {
            self::$validator = new Validator();
            self::$validator->setMaxErrors(1000);
            self::$validator->resolver()->registerRaw(self::schemaJson(), self::SCHEMA_ID);
        }

        return self::$validator;
    }

    // JSON Schema needs objects as stdClass, so arrays are round-tripped through JSON.
    private static function decode(string|array|object $document): mixed
    {
        try {
            $json = is_string($document) ? $document : json_encode($document, JSON_THROW_ON_ERROR);

            return json_decode($json, false, 512, JSON_THROW_ON_ERROR);
        } catch (\JsonException $e) {
            throw new \InvalidArgumentException('Document is not JSON: ' . $e->getMessage(), 0, $e);
        }
    }
}
