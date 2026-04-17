package com.guilherme.emobiliaria.receipt.ui.controller;

import com.guilherme.emobiliaria.shared.exception.UserFacingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReceiptFormControllerAmountParsingTest {

  @Test
  void shouldParseBlankAmountAsZero() {
    assertEquals(0, ReceiptFormController.parseCentsValue("   "));
  }

  @Test
  void shouldParseCommaSeparatedAmountToCents() {
    assertEquals(10025, ReceiptFormController.parseCentsValue("100,25"));
  }

  @Test
  void shouldParseDotSeparatedAmountToCents() {
    assertEquals(10025, ReceiptFormController.parseCentsValue("100.25"));
  }

  @Test
  void shouldThrowUserFacingExceptionForInvalidAmount() {
    UserFacingException exception =
        assertThrows(UserFacingException.class, () -> ReceiptFormController.parseCentsValue("abc"));

    assertEquals("receipt.form.error.amount_invalid", exception.getTranslationKey());
  }
}
