# AVS for Java / JVM

The [AVS](https://github.com/AutoUncle/avs) JSON Schema and a validator. Java 17+, and usable from Kotlin and Scala.

```xml
<!-- repository: https://maven.pkg.github.com/AutoUncle/avs -->
<dependency>
  <groupId>com.autouncle</groupId>
  <artifactId>avs</artifactId>
  <version>0.2.0</version>
</dependency>
```

```java
import com.autouncle.avs.Avs;

List<ValidationError> errors = Avs.validate(json); // empty when valid; e.g. "/vehicle/vin: does not match ..."
Avs.isValid(json);
Avs.validateOrThrow(json);                         // InvalidAvsDocumentException with every error
Avs.schemaJson();                                  // the raw schema
```

The artifact version equals the schema release.
