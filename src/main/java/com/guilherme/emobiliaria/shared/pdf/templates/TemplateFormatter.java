package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.shared.util.MoneyFormatter;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.guilherme.emobiliaria.shared.pdf.PdfTemplate.bold;

class TemplateFormatter {

  private static final String[] ONES_PT =
      {"", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove", "dez", "onze",
          "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove"};
  private static final String[] TENS_PT =
      {"", "", "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta", "oitenta",
          "noventa"};
  private static final String[] HUNDREDS_PT =
      {"", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos", "seiscentos",
          "setecentos", "oitocentos", "novecentos"};

  private static final String[] ONES_EN =
      {"", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven",
          "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen",
          "nineteen"};
  private static final String[] TENS_EN =
      {"", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
  private static final String[] HUNDREDS_EN =
      {"", "one hundred", "two hundred", "three hundred", "four hundred", "five hundred",
          "six hundred", "seven hundred", "eight hundred", "nine hundred"};

  private final ResourceBundle bundle;

  TemplateFormatter(ResourceBundle bundle) {
    this.bundle = bundle;
  }

  private static TemplateFormatter defaultPtBrFormatter() {
    return new TemplateFormatter(
        ResourceBundle.getBundle("messages", Locale.forLanguageTag("pt-BR")));
  }

  // ── Static technical formatters (locale-independent) ──────────────────────

  static String formatCurrency(int centavos) {
    return MoneyFormatter.formatWithSymbol(centavos);
  }

  static String formatDate(LocalDate date) {
    return String.format("%02d/%02d/%d", date.getDayOfMonth(), date.getMonthValue(),
        date.getYear());
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
    return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6,
        9) + "-" + digits.substring(9, 11);
  }

  static String formatCnpj(String digits) {
    return digits.substring(0, 2) + "." + digits.substring(2, 5) + "." + digits.substring(5,
        8) + "/" + digits.substring(8, 12) + "-" + digits.substring(12, 14);
  }

  static String formatCep(String digits) {
    return digits.substring(0, 5) + "-" + digits.substring(5, 8);
  }

  static String personName(Person person) {
    if (person instanceof PhysicalPerson pp)
      return pp.getName();
    if (person instanceof JuridicalPerson jp)
      return jp.getCorporateName();
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  static String personDescription(Person person) {
    if (person instanceof PhysicalPerson pp) {
      return pp.getNationality() + ", " + civilStateInPortuguese(
          pp.getCivilState()) + ", " + pp.getOccupation() + ", CPF " + formatCpf(
          pp.getCpf()) + ", RG " + pp.getIdCardNumber();
    }
    if (person instanceof JuridicalPerson jp) {
      return "CNPJ " + formatCnpj(jp.getCnpj()) + ", representada por " + jp.getRepresentatives()
          .stream().map(PhysicalPerson::getName).collect(java.util.stream.Collectors.joining(", "));
    }
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  static String personCity(Person person) {
    if (person instanceof PhysicalPerson pp)
      return pp.getAddress().getCity();
    if (person instanceof JuridicalPerson jp)
      return jp.getAddress().getCity();
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  static String civilStateInPortuguese(CivilState civilState) {
    return defaultPtBrFormatter().civilStateLabel(civilState);
  }

  // ── Instance locale-aware formatters ──────────────────────────────────────

  String formatDateInFull(LocalDate date) {
    Locale locale = isPt() ? bundle.getLocale() : Locale.ENGLISH;
    return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale));
  }

  String formatCurrencyInFull(int centavos) {
    int reais = centavos / 100;
    if (reais == 0)
      return bundle.getString("formatter.currency.zero");
    if (reais == 1)
      return bundle.getString("formatter.currency.one");
    return numberInWords(reais) + " " + bundle.getString("formatter.currency.suffix");
  }

  String formatPeriod(Period period) {
    int years = period.getYears();
    int months = period.getMonths();
    String and = bundle.getString("formatter.period.and");
    if (years == 0) {
      return months + " " + (months == 1 ?
          bundle.getString("formatter.period.month") :
          bundle.getString("formatter.period.months"));
    }
    if (months == 0) {
      return years + " " + (years == 1 ?
          bundle.getString("formatter.period.year") :
          bundle.getString("formatter.period.years"));
    }
    return years + " " + (years == 1 ?
        bundle.getString("formatter.period.year") :
        bundle.getString(
            "formatter.period.years")) + " " + and + " " + months + " " + (months == 1 ?
        bundle.getString("formatter.period.month") :
        bundle.getString("formatter.period.months"));
  }

  String formatPeriodForContract(Period period) {
    int years = period.getYears();
    int months = period.getMonths();
    String and = bundle.getString("formatter.period.and");
    if (years == 0) {
      return months + " (" + numberInWords(months) + ") " + (months == 1 ?
          bundle.getString("formatter.period.month") :
          bundle.getString("formatter.period.months"));
    }
    if (months == 0) {
      return years + " (" + numberInWords(years) + ") " + (years == 1 ?
          bundle.getString("formatter.period.year") :
          bundle.getString("formatter.period.years"));
    }
    return years + " (" + numberInWords(years) + ") " + (years == 1 ?
        bundle.getString("formatter.period.year") :
        bundle.getString(
            "formatter.period.years")) + " " + and + " " + months + " (" + numberInWords(
        months) + ") " + (months == 1 ?
        bundle.getString("formatter.period.month") :
        bundle.getString("formatter.period.months"));
  }

  String formatAddressForContract(Address address) {
    StringBuilder sb = new StringBuilder();
    sb.append(address.getAddress()).append(" ").append(address.getNumber());
    if (address.getComplement() != null) {
      sb.append(", ").append(address.getComplement());
    }
    sb.append(bundle.getString("formatter.address.neighborhood")).append(" ")
        .append(address.getNeighborhood());
    sb.append(", ").append(address.getCity());
    sb.append("/").append(address.getState().name());
    String cepLabel = bundle.getString("formatter.address.cep");
    sb.append(isPt() ? cepLabel : " " + cepLabel).append(" ").append(formatCep(address.getCep()));
    return sb.toString();
  }

  String civilStateLabel(CivilState civilState) {
    return switch (civilState) {
      case SINGLE -> bundle.getString("formatter.civil_state.single");
      case MARRIED -> bundle.getString("formatter.civil_state.married");
      case DIVORCED -> bundle.getString("formatter.civil_state.divorced");
      case WIDOWER -> bundle.getString("formatter.civil_state.widower");
      case STABLE_UNION -> bundle.getString("formatter.civil_state.stable_union");
    };
  }

  String formatPhysicalPersonForContract(PhysicalPerson pp) {
    return bold(pp.getName()) + ", " + pp.getNationality() + ", " + civilStateLabel(
        pp.getCivilState()) + ", " + pp.getOccupation() + bundle.getString(
        "formatter.person.cpf_prefix") + " " + formatCpf(pp.getCpf()) + bundle.getString(
        "formatter.person.id_card_prefix") + " " + pp.getIdCardNumber() + bundle.getString(
        "formatter.person.resident_at") + " " + formatAddressForContract(pp.getAddress());
  }

  String formatPersonForContract(Person person) {
    if (person instanceof PhysicalPerson pp) {
      return formatPhysicalPersonForContract(pp) + ".";
    }
    if (person instanceof JuridicalPerson jp) {
      return bold(jp.getCorporateName()) + " " + bundle.getString(
          "formatter.person.cnpj_prefix") + " " + formatCnpj(jp.getCnpj()) + bundle.getString(
          "formatter.person.based_at") + " " + formatAddressForContract(
          jp.getAddress()) + bundle.getString(
          "formatter.person.represented_by") + " " + jp.getRepresentatives().stream()
          .map(this::formatPhysicalPersonForContract)
          .collect(java.util.stream.Collectors.joining(" e ")) + ".";
    }
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  String numberInWords(int n) {
    if (isPt())
      return numberInWordsPt(n);
    return numberInWordsEn(n);
  }

  // ── Private helpers ────────────────────────────────────────────────────────

  private boolean isPt() {
    return "pt".equals(bundle.getLocale().getLanguage());
  }

  private String numberInWordsPt(int n) {
    if (n == 100)
      return "cem";
    if (n >= 1000) {
      int thousands = n / 1000;
      int remainder = n % 1000;
      String thousandPart = thousands == 1 ? "mil" : numberInWordsPt(thousands) + " mil";
      if (remainder == 0)
        return thousandPart;
      return thousandPart + " e " + numberInWordsPt(remainder);
    }
    if (n >= 100) {
      int hundreds = n / 100;
      int remainder = n % 100;
      String hundredPart = HUNDREDS_PT[hundreds];
      if (remainder == 0)
        return hundredPart;
      return hundredPart + " e " + numberInWordsPt(remainder);
    }
    if (n >= 20) {
      int tens = n / 10;
      int ones = n % 10;
      if (ones == 0)
        return TENS_PT[tens];
      return TENS_PT[tens] + " e " + ONES_PT[ones];
    }
    return ONES_PT[n];
  }

  private String numberInWordsEn(int n) {
    if (n >= 1000) {
      int thousands = n / 1000;
      int remainder = n % 1000;
      String thousandPart =
          thousands == 1 ? "one thousand" : numberInWordsEn(thousands) + " thousand";
      if (remainder == 0)
        return thousandPart;
      return thousandPart + " and " + numberInWordsEn(remainder);
    }
    if (n >= 100) {
      int hundreds = n / 100;
      int remainder = n % 100;
      String hundredPart = HUNDREDS_EN[hundreds];
      if (remainder == 0)
        return hundredPart;
      return hundredPart + " and " + numberInWordsEn(remainder);
    }
    if (n >= 20) {
      int tens = n / 10;
      int ones = n % 10;
      if (ones == 0)
        return TENS_EN[tens];
      return TENS_EN[tens] + "-" + ONES_EN[ones];
    }
    return ONES_EN[n];
  }
}
