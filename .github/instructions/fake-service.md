---
applyTo: "src/test/**/domain/service/**/*"
---

# Fake Service Implementation Instructions

- Fake service implementations should extend com.guilherme.emobiliaria.shared.fake.FakeImplementation
- Every method should execute maybeFail() at it's start
