package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;

class TemplateFormatter {

  private static final String[] ONES = {
      "", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove",
      "dez", "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete",
      "dezoito", "dezenove"
  };
  private static final String[] TENS = {
      "", "", "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta", "oitenta", "noventa"
  };
  private static final String[] HUNDREDS = {
      "", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos",
      "seiscentos", "setecentos", "oitocentos", "novecentos"
  };
  private static final String[] MONTHS = {
      "janeiro", "fevereiro", "março", "abril", "maio", "junho",
      "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
  };

  static String formatCurrency(int centavos) {
    NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("pt-BR"));
    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);
    return "R$ " + nf.format(centavos / 100.0);
  }

  static String formatCurrencyInFull(int centavos) {
    int reais = centavos / 100;
    if (reais == 0) return "zero reais";
    if (reais == 1) return "um real";
    return numberInWords(reais) + " reais";
  }

  static String formatDate(LocalDate date) {
    return String.format("%02d/%02d/%d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
  }

  static String formatDateInFull(LocalDate date) {
    return date.getDayOfMonth() + " de " + MONTHS[date.getMonthValue() - 1] + " de " + date.getYear();
  }

  static String formatPeriod(Period period) {
    int years = period.getYears();
    int months = period.getMonths();
    if (years == 0) {
      return months + (months == 1 ? " mês" : " meses");
    }
    if (months == 0) {
      return years + (years == 1 ? " ano" : " anos");
    }
    return years + (years == 1 ? " ano" : " anos") + " e " + months + (months == 1 ? " mês" : " meses");
  }

  static String formatAddress(Address address) {
    StringBuilder sb = new StringBuilder();
    sb.append(address.getAddress()).append(", ").append(address.getNumber());
    if (address.getComplement() != null) {
      sb.append(", ").append(address.getComplement());
    }
    sb.append(", ").append(address.getNeighborhood());
    sb.append(", ").append(address.getCity());
    sb.append(" - ").append(address.getState().name());
    return sb.toString();
  }

  static String formatCpf(String digits) {
    return digits.substring(0, 3) + "." + digits.substring(3, 6) + "."
        + digits.substring(6, 9) + "-" + digits.substring(9, 11);
  }

  static String formatCnpj(String digits) {
    return digits.substring(0, 2) + "." + digits.substring(2, 5) + "."
        + digits.substring(5, 8) + "/" + digits.substring(8, 12) + "-" + digits.substring(12, 14);
  }

  static String civilStateInPortuguese(CivilState civilState) {
    return switch (civilState) {
      case SINGLE -> "solteiro";
      case MARRIED -> "casado";
      case DIVORCED -> "divorciado";
      case WIDOWER -> "viúvo";
    };
  }

  static String personName(Person person) {
    if (person instanceof PhysicalPerson pp) return pp.getName();
    if (person instanceof JuridicalPerson jp) return jp.getCorporateName();
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  static String personDescription(Person person) {
    if (person instanceof PhysicalPerson pp) {
      return pp.getNationality() + ", " + civilStateInPortuguese(pp.getCivilState()) + ", "
          + pp.getOccupation() + ", CPF " + formatCpf(pp.getCpf())
          + ", RG " + pp.getIdCardNumber();
    }
    if (person instanceof JuridicalPerson jp) {
      return "CNPJ " + formatCnpj(jp.getCnpj()) + ", representada por " + jp.getRepresentative().getName();
    }
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  static String personCity(Person person) {
    if (person instanceof PhysicalPerson pp) return pp.getAddress().getCity();
    if (person instanceof JuridicalPerson jp) return jp.getAddress().getCity();
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  private static String numberInWords(int n) {
    if (n == 100) return "cem";
    if (n >= 1000) {
      int thousands = n / 1000;
      int remainder = n % 1000;
      String thousandPart = thousands == 1 ? "mil" : numberInWords(thousands) + " mil";
      if (remainder == 0) return thousandPart;
      return thousandPart + " e " + numberInWords(remainder);
    }
    if (n >= 100) {
      int hundreds = n / 100;
      int remainder = n % 100;
      String hundredPart = HUNDREDS[hundreds];
      if (remainder == 0) return hundredPart;
      return hundredPart + " e " + numberInWords(remainder);
    }
    if (n >= 20) {
      int tens = n / 10;
      int ones = n % 10;
      if (ones == 0) return TENS[tens];
      return TENS[tens] + " e " + ONES[ones];
    }
    return ONES[n];
  }
}
