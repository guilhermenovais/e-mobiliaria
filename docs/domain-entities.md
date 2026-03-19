# Domain Entity Instructions

- An entity should never be allowed to exist in an invalid state.
- Business rules should be enforced on setters.
- Factory methods should be used to create entities. Constructors should remain private.
- Factory methods should use setters to set the properties before returning the entity.
- There should be a `restore` factory method to instantiate an existing entity.
- There should be a `create` factory method to create a new entity. This should not receive an ID.
- To enforce business rules, com.guilherme.emobiliaria.shared.exception.BusinessException should be thrown. The message
  should be added to com.guilherme.emobiliaria.shared.exception.ErrorMessage if necessary.
