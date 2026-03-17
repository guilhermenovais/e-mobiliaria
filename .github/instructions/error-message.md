---
applyTo: "src/main/java/com/guilherme/emobiliaria/shared/exception/ErrorMessage.java"
---

# Error Message Instructions

- Error messages should be grouped in enums inside the ErrorMessage class, following the pattern below:

```java
enum EntityName implements ErrorMessage {
  SOME_ENTITY_ERROR("entity.some_error", "Some error message"), ANOTHER_ENTITY_ERROR("entity.another_error",
      "Another error message");

  private final String translationKey;
  private final String logMessage;

  EntityName(String translationKey, String logMessage) {
    this.translationKey = translationKey;
    this.logMessage = logMessage;
  }

  @Override
  public String getTranslationKey() {
    return translationKey;
  }

  @Override
  public String getLogMessage() {
    return logMessage;
  }
}
```

- After an error message is added, the translation key should be added to the
  `src/main/resources/messages.properties` (English) and `src/main/resources/messages_pt_BR.properties` (Portuguese)
  files.

```
