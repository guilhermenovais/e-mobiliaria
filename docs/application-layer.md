# Application Layer Instructions

- For each use case, a class named `<UseCaseName>Interactor` should be created in `usecase/`, a class named
  `<UseCaseName>Input` should be created in `input/` and a class named `<UseCaseName>Output` should be created in
  `output/`. `<UseCaseName>Interactor` should receive the input and return the output.
- The input should contain only the data necessary for the use case to execute.
- The output should contain the results of the use case execution. If the return data is an entity, the entity should be
  returned in a field in the output class.
