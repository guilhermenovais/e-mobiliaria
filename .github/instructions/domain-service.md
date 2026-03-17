---
applyTo: "src/main/**/domain/service/**/*"
---

# Domain Service Instructions

- If the real implementation of the service depends on an external system, or if some of it's method takes a long time
  to execute, only the interface and the fake implementation should be implemented. Example: a service that depends on a
  web API, or a service that generates a PDF file.
- If a fake implementation is needed, it should be placed on the matching test.
  package.
- If the real implementation of the service runs in memory and is fast, only the real implementation should be
  implemented. Example: a service that validates the format of a field.
