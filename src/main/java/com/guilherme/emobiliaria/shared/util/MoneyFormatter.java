package com.guilherme.emobiliaria.shared.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyFormatter {

  private MoneyFormatter() {
  }

  public static String format(int cents) {
    NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);
    return nf.format(cents / 100.0);
  }

  public static String format(long cents) {
    NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);
    return nf.format(cents / 100.0);
  }

  public static String formatWithSymbol(int cents) {
    return "R$ " + format(cents);
  }

  public static String formatWithSymbol(long cents) {
    return "R$ " + format(cents);
  }
}
