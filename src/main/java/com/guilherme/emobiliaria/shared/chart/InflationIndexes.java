package com.guilherme.emobiliaria.shared.chart;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * Monthly rates for Brazilian inflation indexes (IPCA from IBGE, IGP-M from FGV).
 * Values are expressed as decimals (e.g., 0.005 = 0.5%).
 * Used to project rent values forward from an initial date.
 */
public final class InflationIndexes {

  private InflationIndexes() {}

  /** IPCA monthly rates (IBGE). */
  public static final Map<YearMonth, Double> IPCA = buildIpca();

  /** IGP-M monthly rates (FGV). */
  public static final Map<YearMonth, Double> IGP_M = buildIgpm();

  private static Map<YearMonth, Double> buildIpca() {
    Map<YearMonth, Double> m = new HashMap<>();
    // 2018
    m.put(YearMonth.of(2018, 1), 0.0029); m.put(YearMonth.of(2018, 2), 0.0032);
    m.put(YearMonth.of(2018, 3), 0.0009); m.put(YearMonth.of(2018, 4), 0.0022);
    m.put(YearMonth.of(2018, 5), 0.0040); m.put(YearMonth.of(2018, 6), 0.0126);
    m.put(YearMonth.of(2018, 7), 0.0033); m.put(YearMonth.of(2018, 8), -0.0009);
    m.put(YearMonth.of(2018, 9), 0.0048); m.put(YearMonth.of(2018, 10), 0.0045);
    m.put(YearMonth.of(2018, 11), -0.0021); m.put(YearMonth.of(2018, 12), 0.0015);
    // 2019
    m.put(YearMonth.of(2019, 1), 0.0032); m.put(YearMonth.of(2019, 2), 0.0043);
    m.put(YearMonth.of(2019, 3), 0.0075); m.put(YearMonth.of(2019, 4), 0.0057);
    m.put(YearMonth.of(2019, 5), 0.0013); m.put(YearMonth.of(2019, 6), 0.0001);
    m.put(YearMonth.of(2019, 7), 0.0019); m.put(YearMonth.of(2019, 8), 0.0011);
    m.put(YearMonth.of(2019, 9), -0.0004); m.put(YearMonth.of(2019, 10), 0.0010);
    m.put(YearMonth.of(2019, 11), 0.0051); m.put(YearMonth.of(2019, 12), 0.0115);
    // 2020
    m.put(YearMonth.of(2020, 1), 0.0021); m.put(YearMonth.of(2020, 2), 0.0025);
    m.put(YearMonth.of(2020, 3), 0.0007); m.put(YearMonth.of(2020, 4), -0.0031);
    m.put(YearMonth.of(2020, 5), -0.0038); m.put(YearMonth.of(2020, 6), 0.0026);
    m.put(YearMonth.of(2020, 7), 0.0036); m.put(YearMonth.of(2020, 8), 0.0024);
    m.put(YearMonth.of(2020, 9), 0.0064); m.put(YearMonth.of(2020, 10), 0.0086);
    m.put(YearMonth.of(2020, 11), 0.0089); m.put(YearMonth.of(2020, 12), 0.0135);
    // 2021
    m.put(YearMonth.of(2021, 1), 0.0025); m.put(YearMonth.of(2021, 2), 0.0086);
    m.put(YearMonth.of(2021, 3), 0.0093); m.put(YearMonth.of(2021, 4), 0.0031);
    m.put(YearMonth.of(2021, 5), 0.0083); m.put(YearMonth.of(2021, 6), 0.0053);
    m.put(YearMonth.of(2021, 7), 0.0096); m.put(YearMonth.of(2021, 8), 0.0087);
    m.put(YearMonth.of(2021, 9), 0.0116); m.put(YearMonth.of(2021, 10), 0.0125);
    m.put(YearMonth.of(2021, 11), 0.0095); m.put(YearMonth.of(2021, 12), 0.0073);
    // 2022
    m.put(YearMonth.of(2022, 1), 0.0054); m.put(YearMonth.of(2022, 2), 0.0101);
    m.put(YearMonth.of(2022, 3), 0.0162); m.put(YearMonth.of(2022, 4), 0.0106);
    m.put(YearMonth.of(2022, 5), 0.0047); m.put(YearMonth.of(2022, 6), 0.0067);
    m.put(YearMonth.of(2022, 7), -0.0068); m.put(YearMonth.of(2022, 8), -0.0036);
    m.put(YearMonth.of(2022, 9), -0.0029); m.put(YearMonth.of(2022, 10), 0.0059);
    m.put(YearMonth.of(2022, 11), 0.0041); m.put(YearMonth.of(2022, 12), 0.0054);
    // 2023
    m.put(YearMonth.of(2023, 1), 0.0053); m.put(YearMonth.of(2023, 2), 0.0084);
    m.put(YearMonth.of(2023, 3), 0.0071); m.put(YearMonth.of(2023, 4), 0.0061);
    m.put(YearMonth.of(2023, 5), 0.0023); m.put(YearMonth.of(2023, 6), -0.0008);
    m.put(YearMonth.of(2023, 7), 0.0012); m.put(YearMonth.of(2023, 8), 0.0023);
    m.put(YearMonth.of(2023, 9), 0.0026); m.put(YearMonth.of(2023, 10), 0.0024);
    m.put(YearMonth.of(2023, 11), 0.0028); m.put(YearMonth.of(2023, 12), 0.0062);
    // 2024
    m.put(YearMonth.of(2024, 1), 0.0042); m.put(YearMonth.of(2024, 2), 0.0083);
    m.put(YearMonth.of(2024, 3), 0.0016); m.put(YearMonth.of(2024, 4), 0.0038);
    m.put(YearMonth.of(2024, 5), 0.0046); m.put(YearMonth.of(2024, 6), 0.0020);
    m.put(YearMonth.of(2024, 7), 0.0038); m.put(YearMonth.of(2024, 8), 0.0044);
    m.put(YearMonth.of(2024, 9), 0.0044); m.put(YearMonth.of(2024, 10), 0.0056);
    m.put(YearMonth.of(2024, 11), 0.0039); m.put(YearMonth.of(2024, 12), 0.0052);
    // 2025
    m.put(YearMonth.of(2025, 1), 0.0016); m.put(YearMonth.of(2025, 2), 0.0131);
    m.put(YearMonth.of(2025, 3), 0.0056); m.put(YearMonth.of(2025, 4), 0.0043);
    return m;
  }

  private static Map<YearMonth, Double> buildIgpm() {
    Map<YearMonth, Double> m = new HashMap<>();
    // 2018
    m.put(YearMonth.of(2018, 1), 0.0077); m.put(YearMonth.of(2018, 2), 0.0007);
    m.put(YearMonth.of(2018, 3), 0.0064); m.put(YearMonth.of(2018, 4), 0.0057);
    m.put(YearMonth.of(2018, 5), 0.0138); m.put(YearMonth.of(2018, 6), 0.0187);
    m.put(YearMonth.of(2018, 7), -0.0036); m.put(YearMonth.of(2018, 8), -0.0081);
    m.put(YearMonth.of(2018, 9), 0.0050); m.put(YearMonth.of(2018, 10), 0.0112);
    m.put(YearMonth.of(2018, 11), 0.0052); m.put(YearMonth.of(2018, 12), 0.0108);
    // 2019
    m.put(YearMonth.of(2019, 1), 0.0000); m.put(YearMonth.of(2019, 2), 0.0087);
    m.put(YearMonth.of(2019, 3), 0.0116); m.put(YearMonth.of(2019, 4), 0.0092);
    m.put(YearMonth.of(2019, 5), 0.0058); m.put(YearMonth.of(2019, 6), 0.0060);
    m.put(YearMonth.of(2019, 7), 0.0035); m.put(YearMonth.of(2019, 8), -0.0026);
    m.put(YearMonth.of(2019, 9), -0.0007); m.put(YearMonth.of(2019, 10), 0.0023);
    m.put(YearMonth.of(2019, 11), 0.0091); m.put(YearMonth.of(2019, 12), 0.0148);
    // 2020
    m.put(YearMonth.of(2020, 1), 0.0027); m.put(YearMonth.of(2020, 2), 0.0027);
    m.put(YearMonth.of(2020, 3), 0.0001); m.put(YearMonth.of(2020, 4), 0.0080);
    m.put(YearMonth.of(2020, 5), 0.0028); m.put(YearMonth.of(2020, 6), 0.0163);
    m.put(YearMonth.of(2020, 7), 0.0223); m.put(YearMonth.of(2020, 8), 0.0274);
    m.put(YearMonth.of(2020, 9), 0.0434); m.put(YearMonth.of(2020, 10), 0.0323);
    m.put(YearMonth.of(2020, 11), 0.0328); m.put(YearMonth.of(2020, 12), 0.0200);
    // 2021
    m.put(YearMonth.of(2021, 1), 0.0258); m.put(YearMonth.of(2021, 2), 0.0253);
    m.put(YearMonth.of(2021, 3), 0.0294); m.put(YearMonth.of(2021, 4), 0.0153);
    m.put(YearMonth.of(2021, 5), 0.0410); m.put(YearMonth.of(2021, 6), 0.0060);
    m.put(YearMonth.of(2021, 7), 0.0078); m.put(YearMonth.of(2021, 8), 0.0066);
    m.put(YearMonth.of(2021, 9), 0.0131); m.put(YearMonth.of(2021, 10), 0.0064);
    m.put(YearMonth.of(2021, 11), 0.0001); m.put(YearMonth.of(2021, 12), -0.0010);
    // 2022
    m.put(YearMonth.of(2022, 1), 0.0182); m.put(YearMonth.of(2022, 2), 0.0183);
    m.put(YearMonth.of(2022, 3), 0.0174); m.put(YearMonth.of(2022, 4), 0.0141);
    m.put(YearMonth.of(2022, 5), 0.0052); m.put(YearMonth.of(2022, 6), 0.0059);
    m.put(YearMonth.of(2022, 7), 0.0040); m.put(YearMonth.of(2022, 8), -0.0070);
    m.put(YearMonth.of(2022, 9), -0.0095); m.put(YearMonth.of(2022, 10), -0.0097);
    m.put(YearMonth.of(2022, 11), -0.0056); m.put(YearMonth.of(2022, 12), 0.0054);
    // 2023
    m.put(YearMonth.of(2023, 1), 0.0056); m.put(YearMonth.of(2023, 2), -0.0006);
    m.put(YearMonth.of(2023, 3), -0.0015); m.put(YearMonth.of(2023, 4), -0.0006);
    m.put(YearMonth.of(2023, 5), -0.0184); m.put(YearMonth.of(2023, 6), -0.0193);
    m.put(YearMonth.of(2023, 7), -0.0072); m.put(YearMonth.of(2023, 8), 0.0087);
    m.put(YearMonth.of(2023, 9), 0.0052); m.put(YearMonth.of(2023, 10), 0.0053);
    m.put(YearMonth.of(2023, 11), 0.0138); m.put(YearMonth.of(2023, 12), 0.0074);
    // 2024
    m.put(YearMonth.of(2024, 1), 0.0007); m.put(YearMonth.of(2024, 2), 0.0052);
    m.put(YearMonth.of(2024, 3), 0.0047); m.put(YearMonth.of(2024, 4), 0.0089);
    m.put(YearMonth.of(2024, 5), 0.0039); m.put(YearMonth.of(2024, 6), 0.0081);
    m.put(YearMonth.of(2024, 7), 0.0061); m.put(YearMonth.of(2024, 8), 0.0044);
    m.put(YearMonth.of(2024, 9), 0.0062); m.put(YearMonth.of(2024, 10), 0.0152);
    m.put(YearMonth.of(2024, 11), 0.0130); m.put(YearMonth.of(2024, 12), 0.0094);
    // 2025
    m.put(YearMonth.of(2025, 1), 0.0082); m.put(YearMonth.of(2025, 2), 0.0106);
    m.put(YearMonth.of(2025, 3), 0.0056); m.put(YearMonth.of(2025, 4), 0.0096);
    return m;
  }
}
