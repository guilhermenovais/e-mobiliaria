# Fake Repository Implementation Instructions

- Fake repository implementations should extend com.guilherme.emobiliaria.shared.fake.FakeImplementation
- Every method should execute maybeFail() at it's start
- The implementation should maintain a collection of in-memory entities, which should be manipulated according to the
  method's behavior. For example, a save() method should add the entity to the collection, while a delete() method
  should remove it.
- The implementation should handle id generation when needed, using AtomicLong.
- On update and delete methods, if the entity is not found, a
  com.guilherme.emobiliaria.shared.exception.PersistenceException
  should be thrown with the message "<EntityName> not found". An com.guilherme.emobiliaria.shared.exception.ErrorMessage
  for this message should be defined if it still does not exist.
