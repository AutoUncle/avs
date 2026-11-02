<?php

declare(strict_types=1);

namespace AutoUncle\Avs;

/** One reason a document is invalid. */
final class ValidationError
{
    /**
     * @param string $pointer JSON Pointer to the offending value, "" for the document itself
     */
    public function __construct(
        public readonly string $pointer,
        public readonly string $message,
    ) {
    }

    public function __toString(): string
    {
        return ($this->pointer === '' ? '/' : $this->pointer) . ': ' . $this->message;
    }
}
