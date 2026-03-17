---
applyTo: "src/main/**/architecture/repository/**/*"
---

# Repository Implementation Instructions

- Exceptions should be mapped to a com.guilherme.emobiliaria.shared.exception.PersistenceException, with the
  com.guilherme.emobiliaria.shared.exception.ErrorMessage containing an user friendly explanation of why the operation
  failed.
