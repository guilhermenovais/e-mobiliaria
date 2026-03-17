---
applyTo: "src/main/**/*.java"
---

# Test Instructions

- Tests should be written using JUnit 5.
- Test methods should be named `should<ExpectedBehavior>When<Condition>`, with concise ExpectedBehavior and Condition.
- All test methods should be annotated with `@DisplayName` to provide a clear description of the test case condition and
  expected behavior. It should be in the format `When <DescriptionOfCondition>, should <DescriptionOfExpectedBehavior>`.
- Tests should be grouped by the method they are testing using the `@Nested` annotation to create inner test classes.
  Each inner class should be named after the method being tested.
- Tests should use the Arrange-Act-Assert structure.
- Assertions should be made using `org.junit.jupiter.api.Assertions`.
- `@ParameterizedTest` should be used for behavioral variations, with the `name` parameter set for documentation
  clarity.
- You should assert behavior and state, not implementation. Never assert that a method was called.
- When the class under test depends on a repository or service, you should look if a fake implementation exists. Fake
  implementations are named `Fake<ClassName>`. You should use a real implementation only if there is no fake
  implementation.
- Every fake implementation implements com.guilherme.emobiliaria.shared.fake.FakeImplementation, so it's possible to
  call `failNext` method, which will make the next call to a method in the fake implementation fail.
