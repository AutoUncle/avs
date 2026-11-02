# AVS for PHP

The [AVS](https://github.com/AutoUncle/avs) JSON Schema and a validator. PHP 8.1+.

```json
"repositories": [{ "type": "vcs", "url": "https://github.com/AutoUncle/avs" }],
"require": { "autouncle/avs": "^0.2" }
```

```php
use AutoUncle\Avs\Avs;

Avs::validate($json);          // [] or [ValidationError("/vehicle/vin", "The string should match pattern: ...")]
Avs::isValid($json);
Avs::validateOrThrow($json);   // InvalidAvsDocument with every error
```

`$json` can be a JSON string, a decoded object or an associative array. Composer reads `composer.json` from the repository root, so versions are the repository's `vX.Y.Z` tags.
