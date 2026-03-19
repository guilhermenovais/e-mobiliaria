# Domain Repository Instructions

- Every domain repository interface should have a fake implementation, which should be placed on the matching test
  package.
- On methods that return a collection, the method should have a
  com.guilherme.emobiliaria.shared.persistence.PaginationInput parameter and return a
  com.guilherme.emobiliaria.shared.persistence.PagedResult
