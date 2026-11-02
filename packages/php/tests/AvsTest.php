<?php

declare(strict_types=1);

namespace AutoUncle\Avs\Tests;

use AutoUncle\Avs\Avs;
use AutoUncle\Avs\InvalidAvsDocument;
use PHPUnit\Framework\TestCase;

final class AvsTest extends TestCase
{
    private const ROOT = __DIR__ . '/../../..';

    /** @return list<string> */
    private static function json(string $dir): array
    {
        $files = glob(self::ROOT . "/{$dir}/*.json") ?: [];
        self::assertNotEmpty($files, "{$dir} is empty");

        return $files;
    }

    public function testExamplesAreValid(): void
    {
        foreach (self::json('examples') as $f) {
            self::assertSame([], array_map('strval', Avs::validate(file_get_contents($f))), $f);
        }
    }

    public function testInvalidFixturesAreRejected(): void
    {
        foreach (self::json('tests/invalid') as $f) {
            self::assertFalse(Avs::isValid(file_get_contents($f)), "{$f} should be rejected");
        }
    }

    public function testReportsPointerForBadVin(): void
    {
        $doc = json_decode(file_get_contents(self::ROOT . '/examples/vehicle-bmw-320e-g20.json'), true);
        $doc['vehicle']['vin'] = 'NOT-A-VIN';
        $errors = Avs::validate($doc);
        self::assertNotEmpty($errors);
        self::assertSame(['/vehicle/vin'], array_values(array_unique(array_map(fn ($e) => $e->pointer, $errors))), implode("\n", $errors));
    }

    public function testValidateOrThrowListsErrors(): void
    {
        try {
            Avs::validateOrThrow('{}');
            self::fail('expected InvalidAvsDocument');
        } catch (InvalidAvsDocument $e) {
            self::assertNotEmpty($e->errors);
        }
    }

    public function testMalformedJsonIsRejected(): void
    {
        self::assertFalse(Avs::isValid('{'));
        $this->expectException(\InvalidArgumentException::class);
        Avs::validate('{');
    }

    public function testSchemaVersionMatchesVersionFile(): void
    {
        $v = explode('.', trim(file_get_contents(self::ROOT . '/VERSION')));
        self::assertSame("{$v[0]}.{$v[1]}", Avs::schemaVersion());
    }
}
