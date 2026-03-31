package com.guilherme.emobiliaria.shared.ui.component;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class MaskedTextField extends TextField {

  private final String mask;
  private final int maxDigits;

  public MaskedTextField(String mask) {
    this.mask = mask;
    this.maxDigits = (int) mask.chars().filter(c -> c == '0').count();
    getStyleClass().add("form-input");
    setMaxWidth(Double.MAX_VALUE);
    setTextFormatter(new TextFormatter<>(change -> {
      String proposed = change.getControlNewText();
      String digits = proposed.replaceAll("[^0-9]", "");
      if (digits.length() > maxDigits) {
        digits = digits.substring(0, maxDigits);
      }
      String formatted = applyMask(digits);
      change.setText(formatted);
      change.setRange(0, change.getControlText().length());
      change.setCaretPosition(formatted.length());
      change.setAnchor(formatted.length());
      return change;
    }));
  }

  public String getValue() {
    return getText().replaceAll("[^0-9]", "");
  }

  private String applyMask(String digits) {
    StringBuilder result = new StringBuilder();
    int digitIndex = 0;
    for (int i = 0; i < mask.length() && digitIndex < digits.length(); i++) {
      char maskChar = mask.charAt(i);
      if (maskChar == '0') {
        result.append(digits.charAt(digitIndex++));
      } else {
        result.append(maskChar);
      }
    }
    return result.toString();
  }
}
