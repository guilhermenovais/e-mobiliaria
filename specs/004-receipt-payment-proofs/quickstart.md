# Quickstart: Implementing Receipt Payment Proofs

## Build & Run

```bash
# Compile and run tests
mvn test

# Run the app
mvn javafx:run
```

## Implementation Order

Follow this order to keep the build green at each step:

1. **DB migration** → `V10__add_payment_proofs.sql`
2. **Domain entity** → `PaymentProof`, `ProofFileType` enum
3. **Domain interfaces** → `PaymentProofRepository`, `PaymentProofStorageService`
4. **`Receipt` entity update** → add `proofs` field + `hasProofs()` + `restoreWithProofs()`
5. **Infrastructure** → `JdbcPaymentProofRepository`, `PaymentProofStorageServiceImpl`
6. **`AppDataPaths`** → add `proofStorageDirectory()`
7. **Application use cases** → `AttachPaymentProofInteractor`, `RemovePaymentProofInteractor`,
   `FindPaymentProofsByReceiptIdInteractor`
8. **Update `DeleteReceiptInteractor`** → delete proofs before receipt
9. **DI module** → update `ReceiptModule` with new bindings
10. **`module-info.java`** → open new packages to Guice/FXML
11. **UI — `ProofDropZonePane`** component
12. **UI — `ProofSelectionDialog`** component
13. **UI — update `ReceiptFormController`** to integrate drop zone
14. **UI — update `ReceiptListController`** to add proof button to `ActionsCell`
15. **i18n** → add keys to `messages*.properties`

## Key File Locations

| What                             | Where                                                                                 |
|----------------------------------|---------------------------------------------------------------------------------------|
| Existing `Receipt` entity        | `receipt/domain/entity/Receipt.java`                                                  |
| Existing `ReceiptRepository`     | `receipt/domain/repository/ReceiptRepository.java`                                    |
| Existing `ReceiptModule`         | `receipt/di/ReceiptModule.java`                                                       |
| Existing `ReceiptFormController` | `receipt/ui/controller/ReceiptFormController.java`                                    |
| Existing `ReceiptListController` | `receipt/ui/controller/ReceiptListController.java`                                    |
| `AppDataPaths`                   | `shared/persistence/AppDataPaths.java`                                                |
| DB migrations                    | `src/main/resources/db/migration/`                                                    |
| i18n bundle                      | `src/main/resources/messages.properties` (and `messages_pt_BR.properties` if present) |
| `module-info.java`               | `src/main/java/module-info.java`                                                      |

## Test Conventions

- Domain entity tests: `receipt/domain/entity/PaymentProofTest.java` (JUnit 5, no frameworks)
- Fake repository: `receipt/domain/repository/FakePaymentProofRepository.java` (in-memory)
- Fake service: `receipt/domain/service/FakePaymentProofStorageService.java`
- Use case tests: `receipt/application/usecase/AttachPaymentProofInteractorTest.java`, etc.
- JDBC integration tests: `receipt/infrastructure/repository/JdbcPaymentProofRepositoryTest.java` (uses H2 in-memory +
  Flyway, same pattern as `JdbcPaymentAccountRepositoryTest`)

## Accepted File Extensions

```java
// In ProofFileType.fromExtension(String ext):
PDF   → ".pdf"
IMAGE → ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff", ".tif"
```

## File Storage Pattern

```java
// Attach flow:
String uuid = UUID.randomUUID().toString();
String ext = extractExtension(originalFileName); // ".pdf", ".jpg", etc.
String storedFileName = uuid + ext;
Path dest = AppDataPaths.proofStorageDirectory().resolve(storedFileName);
Files.copy(sourceFile, dest, StandardCopyOption.REPLACE_EXISTING);

// Open flow:
Path file = AppDataPaths.proofStorageDirectory().resolve(proof.getStoredFileName());
Desktop.getDesktop().open(file.toFile()); // wrap in Task
```
