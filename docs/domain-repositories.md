# Domain Repository Instructions

- Every domain repository interface should have a fake implementation, which should be placed on the matching test
  package.
- On methods that return a collection, the method should have a
  com.guilherme.emobiliaria.shared.persistence.PaginationInput parameter and return a
  com.guilherme.emobiliaria.shared.persistence.PagedResult
- Repositories should have separate save and update methods
- On find all by <another entity> methods, the method should be named findAllBy<AnotherEntity>Id, and the parameter
  should be <anotherEntity>Id
