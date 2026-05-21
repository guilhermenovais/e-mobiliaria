package com.guilherme.emobiliaria.shared.util;

public class TaxIdFormatter {

  private TaxIdFormatter() {
  }

  public static String format(String taxId) {
    if (taxId == null)
      return "";
    String digits = taxId.replaceAll("[^0-9]", "");
    if (digits.length() == 11)
      return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6,
          9) + "-" + digits.substring(9, 11);
    if (digits.length() == 14)
      return digits.substring(0, 2) + "." + digits.substring(2, 5) + "." + digits.substring(5,
          8) + "/" + digits.substring(8, 12) + "-" + digits.substring(12, 14);
    return taxId;
  }
}
