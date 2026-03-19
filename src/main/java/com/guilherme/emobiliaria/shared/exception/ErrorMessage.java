package com.guilherme.emobiliaria.shared.exception;

public interface ErrorMessage {
  String getTranslationKey();

  String getLogMessage();

  enum PhysicalPerson implements ErrorMessage {
    NAME_EMPTY("physical_person.name_empty", "Name must not be empty"),
    NAME_TOO_LONG("physical_person.name_too_long", "Name must contain less than 100 characters"),
    NATIONALITY_EMPTY("physical_person.nationality_empty", "Nationality must not be empty"),
    NATIONALITY_TOO_LONG("physical_person.nationality_too_long",
        "Nationality must contain less than 20 characters"),
    OCCUPATION_EMPTY("physical_person.occupation_empty", "Occupation must not be empty"),
    OCCUPATION_TOO_LONG("physical_person.occupation_too_long",
        "Occupation must contain less than 20 characters"),
    CPF_INVALID("physical_person.cpf_invalid", "CPF is invalid"),
    ID_CARD_NUMBER_EMPTY("physical_person.id_card_number_empty",
        "ID card number must not be empty"),
    ID_CARD_NUMBER_TOO_LONG("physical_person.id_card_number_too_long",
        "ID card number must contain less than 20 characters"),
    CIVIL_STATE_NULL("physical_person.civil_state_null", "Civil state must not be null"),
    ADDRESS_NULL("physical_person.address_null", "Address must not be null");

    private final String translationKey;
    private final String logMessage;

    PhysicalPerson(String translationKey, String logMessage) {
      this.translationKey = translationKey;
      this.logMessage = logMessage;
    }

    @Override
    public String getTranslationKey() {
      return translationKey;
    }

    @Override
    public String getLogMessage() {
      return logMessage;
    }
  }

  enum JuridicalPerson implements ErrorMessage {
    CORPORATE_NAME_EMPTY("juridical_person.corporate_name_empty",
        "Corporate name must not be empty"),
    CORPORATE_NAME_TOO_LONG("juridical_person.corporate_name_too_long",
        "Corporate name must contain less than 100 characters"),
    CNPJ_INVALID("juridical_person.cnpj_invalid", "CNPJ is invalid"),
    REPRESENTATIVE_NULL("juridical_person.representative_null",
        "Representative must not be null"),
    ADDRESS_NULL("juridical_person.address_null", "Address must not be null");

    private final String translationKey;
    private final String logMessage;

    JuridicalPerson(String translationKey, String logMessage) {
      this.translationKey = translationKey;
      this.logMessage = logMessage;
    }

    @Override
    public String getTranslationKey() {
      return translationKey;
    }

    @Override
    public String getLogMessage() {
      return logMessage;
    }
  }


  enum Property implements ErrorMessage {
    NAME_EMPTY("property.name_empty", "Name must not be empty"), TYPE_EMPTY("property.type_empty",
        "Type must not be empty"), PURPOSE_NULL("property.purpose_null",
        "Purpose must not be null"), RENT_NEGATIVE("property.rent_negative",
        "Rent must not be negative"), CEMIG_EMPTY("property.cemig_empty",
        "CEMIG must not be empty"), COPASA_EMPTY("property.copasa_empty",
        "COPASA must not be empty"), IPTU_EMPTY("property.iptu_empty",
        "IPTU must not be empty"), ADDRESS_NULL("property.address_null",
        "Address must not be null");

    private final String translationKey;
    private final String logMessage;

    Property(String translationKey, String logMessage) {
      this.translationKey = translationKey;
      this.logMessage = logMessage;
    }

    @Override
    public String getTranslationKey() {
      return translationKey;
    }

    @Override
    public String getLogMessage() {
      return logMessage;
    }
  }


  enum Contract implements ErrorMessage {
    START_DATE_NULL("contract.start_date_null", "Start date must not be null"), DURATION_NULL(
        "contract.duration_null", "Duration must not be null"), PAYMENT_DAY_INVALID(
        "contract.payment_day_invalid",
        "Payment day must be between 1 and 31"), PAYMENT_ACCOUNT_NULL(
        "contract.payment_account_null", "Payment account must not be null"), PROPERTY_NULL(
        "contract.property_null", "Property must not be null"), LANDLORD_NULL(
        "contract.landlord_null", "Landlord must not be null"), TENANTS_EMPTY(
        "contract.tenants_empty", "Tenants list must not be empty");

    private final String translationKey;
    private final String logMessage;

    Contract(String translationKey, String logMessage) {
      this.translationKey = translationKey;
      this.logMessage = logMessage;
    }

    @Override
    public String getTranslationKey() {
      return translationKey;
    }

    @Override
    public String getLogMessage() {
      return logMessage;
    }
  }


  enum PaymentAccount implements ErrorMessage {
    BANK_EMPTY("payment_account.bank_empty", "Bank must not be empty"), BANK_BRANCH_EMPTY(
        "payment_account.bank_branch_empty", "Bank branch must not be empty"), ACCOUNT_NUMBER_EMPTY(
        "payment_account.account_number_empty", "Account number must not be empty");

    private final String translationKey;
    private final String logMessage;

    PaymentAccount(String translationKey, String logMessage) {
      this.translationKey = translationKey;
      this.logMessage = logMessage;
    }

    @Override
    public String getTranslationKey() {
      return translationKey;
    }

    @Override
    public String getLogMessage() {
      return logMessage;
    }
  }

  enum Address implements ErrorMessage {
    CEP_REQUIRED("address.cep_required", "CEP must not be empty"),
    CEP_INVALID("address.cep_invalid", "CEP must contain exactly 8 digits"),
    STREET_REQUIRED("address.street_required", "Street must not be empty"),
    STREET_INVALID_LENGTH("address.street_invalid_length",
        "Street must contain between 3 and 150 characters"),
    STREET_INVALID_CHARACTERS("address.street_invalid_characters",
        "Street contains invalid characters"),
    NUMBER_REQUIRED("address.number_required", "Number must not be empty"),
    NUMBER_TOO_LONG("address.number_too_long", "Number must contain at most 10 characters"),
    NUMBER_INVALID("address.number_invalid", "Number is invalid"),
    COMPLEMENT_WHITESPACE_ONLY("address.complement_whitespace_only",
        "Complement must not be whitespace only"),
    COMPLEMENT_TOO_LONG("address.complement_too_long",
        "Complement must contain at most 100 characters"),
    COMPLEMENT_INVALID_CHARACTERS("address.complement_invalid_characters",
        "Complement contains invalid characters"),
    NEIGHBORHOOD_REQUIRED("address.neighborhood_required", "Neighborhood must not be empty"),
    NEIGHBORHOOD_INVALID_LENGTH("address.neighborhood_invalid_length",
        "Neighborhood must contain between 2 and 100 characters"),
    NEIGHBORHOOD_INVALID_CHARACTERS("address.neighborhood_invalid_characters",
        "Neighborhood contains invalid characters"),
    CITY_REQUIRED("address.city_required", "City must not be empty"),
    CITY_INVALID_LENGTH("address.city_invalid_length",
        "City must contain between 2 and 100 characters"),
    CITY_INVALID_CHARACTERS("address.city_invalid_characters", "City contains invalid characters"),
    STATE_REQUIRED("address.state_required", "State must not be null");

    private final String translationKey;
    private final String logMessage;

    Address(String translationKey, String logMessage) {
      this.translationKey = translationKey;
      this.logMessage = logMessage;
    }

    @Override
    public String getTranslationKey() {
      return translationKey;
    }

    @Override
    public String getLogMessage() {
      return logMessage;
    }
  }
}
