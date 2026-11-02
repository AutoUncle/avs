<?php

declare(strict_types=1);

namespace AutoUncle\Avs;

/** Thrown by {@see Avs::validateOrThrow()}; lists every validation error. */
final class InvalidAvsDocument extends \InvalidArgumentException
{
    /** @param list<ValidationError> $errors */
    public function __construct(public readonly array $errors)
    {
        parent::__construct("Invalid AVS document:\n" . implode("\n", array_map(fn ($e) => "  {$e}", $errors)));
    }
}
