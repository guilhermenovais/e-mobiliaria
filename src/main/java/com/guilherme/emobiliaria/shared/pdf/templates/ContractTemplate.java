package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

public class ContractTemplate
    extends PdfTemplate<ContractTemplate.ContractParameters, ContractTemplate.ContractCollections> {

  private static final String CONTRACTUAL_TERMS_TEXT =
      "Os signatários deste instrumento, devidamente qualificados, tem entre si, justo e acertado o presente contrato de locação, mediante as cláusulas e condições a seguir estipuladas e aceitas.<br><br>" + bold(
          "PRIMEIRA") + " - O prazo desta locação é constante no início deste contrato. No término indicado, o locatário se obriga a entregar o imóvel livre e desembaraçado de coisas e pessoas, no estado em que o recebeu, independentemente de notificação ou interpelação judicial, ressalvada a hipótese de prorrogação de locação, o que somente se fará por escrito.<br><br>" + bold(
          "PARAGRAFO ÚNICO") + " - Caso o locatário não restitua o imóvel no fim do prazo contratual, pagará enquanto estiver na posse do mesmo o aluguel mensal e ajustado nos termos da cláusula Décima-Sétima, até a efetiva desocupação do imóvel objeto deste instrumento.<br><br>" + bold(
          "SEGUNDA") + " - Todos os impostos e taxas que atualmente recaem sobre imóvel locado, bem como qualquer aumento dos mesmos, ou novos que venham a ser criados pelo poder público, serão da inteira responsabilidade do locatário, que se obriga a pagá-los ao locador para que este os liquide em seus respectivos vencimentos. São ainda de responsabilidade do locatário as contas de água, luz, força e gás, assim como as despesas de condomínio, se houver.<br><br>" + bold(
          "PARÁGRAFO PRIMEIRO") + " - O locatário será responsável pelas despesas e multas decorrentes de eventuais retenções dos avisos de impostos, taxas e outros que já incidem ou venham a incidir sobre o imóvel objeto da presente locação.<br><br>" + bold(
          "PARÁGRAFO SEGUNDO") + " - Os recibos referentes aos impostos e taxas serão entregues juntamente com aluguel correspondente ao mês, fazendo parte integrante do mesmo.<br><br>" + bold(
          "TERCEIRA") + " - A falta de pagamento, nas épocas supra determinadas, dos aluguéis e encargos, por si só constituirá o locatário em mora, independentemente de qualquer notificação, interpelação ou aviso extrajudicial.<br><br>" + bold(
          "PARÁGRAFO ÚNICO") + " - Após o vencimento do aluguel, será cobrada a multa de 10%. Ultrapassando 5 dias será cobrado mais 0,2% ao dia. Após 30 dias, será encaminhado ao departamento Jurídico da Empresa.<br><br>" + bold(
          "QUARTA") + " - Executada as obras ou reparações que sejam necessárias à segurança do imóvel, obriga-se o locatário pelas demais, devendo manter o imóvel locado e seus pertencentes, que ora recebe, em perfeito estado de funcionamento, conservação e limpeza, notadamente as instalações sanitárias e elétricas, vidros e pintura, fato que é comprovado pelo locatário e seu fiador.<br><br>" + bold(
          "QUINTA - Todas as benfeitorias que forem feitas, excluídas naturalmente as de instalações de natureza profissional e móveis, ficarão integrados ao imóvel, sem que, por elas, tenham o locatário direito a qualquer indenização ou pagamento. A introdução de tais benfeitorias dependerá da autorização por escrito do locador.<br><br>") + bold(
          "PARÁGRAFO ÚNICO") + " - Quando do término da locação, o locatário restituirá o imóvel nas mesmas condições em que o recebe agora, ficando desde já convencionado que se não fizer, o locador estará autorizado a mandar executar todos os reparos necessários, cobrando do locatário a importância gasta, como encargos da locação.<br><br>" + bold(
          "SEXTA") + " - Faz parte integrante do presente contrato o regulamento interno do prédio, no caso de condomínio, que o locador reconhece e aceita.<br><br>" + bold(
          "SÉTIMA") + " - É expressamente vedado ao locatário sublocar o imóvel no todo ou em parte, cedê-lo a terceiros, seja a título gratuito ou oneroso, transferir o contrato ou dar destinação diversa do uso ou finalidade previstos neste contrato, sem prévia anuência por escrito do locador.<br><br>" + bold(
          "OITAVA") + " - No caso de desapropriação do imóvel objeto deste contrato, o locador e seus administradores e/ou procuradores ficarão exonerados de toda e qualquer responsabilidade decorrente deste contrato, ressalvando-se ao locatário a faculdade de agir tão somente contra o poder expropriante.<br><br>" + bold(
          "NONA") + " - Fica o locador, por si ou por seus prepostos autorizado a vistoriar o imóvel sempre que julgar conveniente.<br><br>" + bold(
          "DÉCIMA") + " - O locatário se obriga a satisfazer, por sua conta exclusiva, a qualquer exigência dos poderes públicos, em razão da atividade exercida no imóvel, assumindo toda a responsabilidade por quaisquer infrações em que incorrer a esse propósito, por inobservância das determinações das autoridades competentes.<br><br>" + bold(
          "DÉCIMA-PRIMEIRA") + " - O locatário declara, neste ato, ter pleno conhecimento de que o resgate de recibos posteriores não significa nem representa quitação de outras obrigações estipuladas no presente contrato, deixadas de cobrar nas épocas certas, principalmente os encargos fixados neste contrato.<br><br>" + bold(
          "DÉCIMA-SEGUNDA") + " - Se o locador admitir, em benefício do locatário, qualquer atraso no pagamento do aluguel e demais despesas que lhe incuba, ou no cumprimento de qualquer outra obrigação contratual, essa tolerância não poderá ser considerada como alteração das condições deste contrato, nem dará ensejo à invocação do Artigo 1.503- inciso I do código Civil Brasileiro por parte do fiador, pois constituirá em ato de mera liberalidade do locador.<br><br>" + bold(
          "DÉCIMA-TERCEIRA") + " - Tudo que for devido em razão deste contrato, será cobrado em processo executivo ou em ação apropriada, no foro da situação do imóvel, com renúncia de qualquer outro, por mais privilegiado que seja, correndo por conta da parte vencida, além do principal e da multa estipulada na Cláusula Décima-Quarta, todas despesas judiciais e extrajudiciais, mais 20% de honorários advocatícios.<br><br>" + bold(
          "DÉCIMA-QUARTA") + " - Fica estipulada a multa de 3 (três) aluguéis à época da infração, na qual incorrerá a parte que infringir qualquer uma das cláusulas deste contrato, ressalvada a parte inocente o direito de poder considerar simultaneamente reincidida a locação, independentemente de qualquer outra formalidade judicial ou extrajudicial. A multa será sempre paga integralmente, seja qual for o prazo já decorrido do presente contrato, ficando claro que o pagamento dessa multa não exime o pagamento dos aluguéis atrasados, além das despesas inerentes ao caso.<br><br>" + bold(
          "DÉCIMA-QUINTA") + " - Como garantia assina também na qualidade de fiador(es) o qualificado no início deste contrato, sendo solidário com locatário em todas as obrigações aqui assumidas.<br><br>" + bold(
          "PARÁGRAFO ÚNICO") + " - Fica desde já expressamente convencionado que, em qualquer hipótese, a responsabilidade do(s) fiador(es) permanecerá integral, sem solução de continuidade e sem limitação de tempo, sempre e até real efetiva entrega do imóvel, em igualdade de condições com afiançado, também na hipótese de prorrogar-se a presente locação, abrindo mão, desde já fiador da faculdade de exoneração prevista no Artigo 1.500 do Código Civil Brasileiro.<br><br>" + bold(
          "DÉCIMA-SEXTA") + " - No caso de morte, falência ou insolvência do fiador, o locatário se obriga a apresentar dentro de 30 (trinta) dias, substituto idôneo, a juízo do locador sob pena de incorrer nas sanções previstas na Cláusula Décima-Quarta do presente contrato.<br><br>" + bold(
          "DÉCIMA-SÉTIMA") + " - O aluguel será reajustado anualmente pelo maior índice oficial, sendo certo que o prazo de reajuste poderá ser alterado pelas partes, desde que permitido pelas Autoridades Federais.<br><br>" + bold(
          "OBS: ") + "Em caso de atraso no aluguel, enviaremos para o SPC, locatários, fiadores e suas respectivas esposas.<br><br>" + "E, por estarem justas, contratadas, cientes e de acordo com todas as cláusulas e condições do presente contrato de locação, as partes por si, seus herdeiros e sucessores assinam este instrumento de 2 (duas) vias para um só efeito, na presença de testemunhas abaixo.";

  private final Contract contract;

  @Override
  public EnumMap<ContractParameters, Object> getParameters() {
    Property property = contract.getProperty();
    PaymentAccount account = contract.getPaymentAccount();
    LocalDate endDate = contract.getStartDate().plus(contract.getDuration()).minusDays(1);

    String tenantsText = buildOrdinalPartiesText(contract.getTenants(), "LOCATÁRIO(A)",
        TemplateFormatter::formatPersonForContract);
    String guarantorsText = buildOrdinalPartiesText(contract.getGuarantors(), "FIADOR(A)",
        TemplateFormatter::formatPersonForContract);
    String witnessesText = buildOrdinalPartiesText(contract.getWitnesses(), "TESTEMUNHA",
        TemplateFormatter::formatPersonForContract);
    String allPartiesText = tenantsText
        + (guarantorsText.isEmpty() ? "" : "<br>" + guarantorsText)
        + (witnessesText.isEmpty() ? "" : "<br>" + witnessesText);

    String paymentMethodText = bold(
        "Dia de pagamento: ") + contract.getPaymentDay() + " (" + TemplateFormatter.numberInWords(
        contract.getPaymentDay()) + "), conta para pagamento: " + account.getBank() + ", agência n° " + account.getBankBranch() + ", conta corrente n° " + account.getAccountNumber() + (
        account.getPixKey() != null ?
            ", chave PIX " + account.getPixKey() :
            "");

    EnumMap<ContractParameters, Object> params = new EnumMap<>(ContractParameters.class);
    params.put(ContractParameters.CONTRACT_TITLE, "CONTRATO DE LOCAÇÃO");
    params.put(ContractParameters.LANDLORD_TEXT,
        bold("LOCADOR: ") + TemplateFormatter.formatPersonForContract(contract.getLandlord()));
    params.put(ContractParameters.TENANTS_TEXT, allPartiesText);
    params.put(ContractParameters.PROPERTY_SECTION_TITLE, "IMÓVEL OBJETO DESTA LOCAÇÃO:");
    params.put(ContractParameters.PROPERTY_TYPE_TEXT, bold("Tipo: ") + property.getType());
    params.put(ContractParameters.PROPERTY_ADDRESS_TEXT,
        bold("Endereço: ") + TemplateFormatter.formatAddressForContract(property.getAddress()));
    params.put(ContractParameters.PROPERTY_PURPOSE_TEXT,
        bold("Uso ou finalidade: ") + contract.getPurpose());
    params.put(ContractParameters.CONTRACT_RENT_TEXT,
        bold("Valor do aluguel mensal: ") + TemplateFormatter.formatCurrency(
            contract.getRent()) + " (" + TemplateFormatter.formatCurrencyInFull(
            contract.getRent()) + ")");
    params.put(ContractParameters.PROPERTY_CEMIG_TEXT,
        "CEMIG: " + property.getCemig() + " - COPASA: " + property.getCopasa());
    params.put(ContractParameters.PROPERTY_IPTU_TEXT,
        "ÍNDICE CADASTRAL (IPTU): " + property.getIptu());
    params.put(ContractParameters.PERIOD_SECTION_TITLE, "PRAZO DESTA LOCAÇÃO:");
    params.put(ContractParameters.CONTRACT_PERIOD_TEXT,
        bold("Período: ") + TemplateFormatter.formatPeriodForContract(contract.getDuration()));
    params.put(ContractParameters.CONTRACT_START_DATE_TEXT,
        bold("Início: ") + TemplateFormatter.formatDateInFull(contract.getStartDate()));
    params.put(ContractParameters.CONTRACT_END_DATE_TEXT,
        bold("Término: ") + TemplateFormatter.formatDateInFull(endDate));
    params.put(ContractParameters.CONTRACT_PAYMENT_METHOD_TEXT, paymentMethodText);
    params.put(ContractParameters.CONTRACTUAL_TERMS_SECTION_TITLE, "CLÁUSULAS CONTRATUAIS");
    params.put(ContractParameters.CONTRACTUAL_TERMS_TEXT, CONTRACTUAL_TERMS_TEXT);
    params.put(ContractParameters.CITY_AND_DATE_TEXT, TemplateFormatter.personCity(
        contract.getLandlord()) + ", " + TemplateFormatter.formatDateInFull(
        contract.getStartDate()) + ".");
    return params;
  }

  @Override
  public EnumMap<ContractCollections, Collection<Object>> getCollections() {
    List<Object> signingEntries = new ArrayList<>();
    signingEntries.add(new TextBean(signingText(contract.getLandlord())));
    contract.getTenants().forEach(t -> signingEntries.add(new TextBean(signingText(t))));
    contract.getGuarantors().forEach(g -> signingEntries.add(new TextBean(signingText(g))));
    contract.getWitnesses().forEach(w -> signingEntries.add(new TextBean(signingText(w))));

    EnumMap<ContractCollections, Collection<Object>> collections =
        new EnumMap<>(ContractCollections.class);
    collections.put(ContractCollections.SIGNING_TEXTS, signingEntries);
    return collections;
  }

  private String signingText(Person person) {
    if (person instanceof PhysicalPerson pp) {
      return pp.getName() + ", CPF: " + TemplateFormatter.formatCpf(pp.getCpf());
    }
    if (person instanceof JuridicalPerson jp) {
      return jp.getCorporateName() + ", CNPJ: " + TemplateFormatter.formatCnpj(jp.getCnpj());
    }
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  private <T> String buildOrdinalPartiesText(List<T> parties, String label,
      Function<T, String> formatter) {
    boolean shouldShowOrdinal = parties.size() > 1;
    return IntStream.range(0, parties.size())
        .mapToObj(i -> {
          String indexedLabel = shouldShowOrdinal ? label + " " + (i + 1) : label;
          return bold(indexedLabel + ": ") + formatter.apply(parties.get(i));
        })
        .collect(Collectors.joining("<br>"));
  }

  public ContractTemplate(Contract contract) {
    super("contract");
    this.contract = contract;
  }

  public enum ContractParameters {
    CONTRACT_TITLE, LANDLORD_TEXT, TENANTS_TEXT, PROPERTY_SECTION_TITLE, PROPERTY_TYPE_TEXT, PROPERTY_ADDRESS_TEXT, PROPERTY_PURPOSE_TEXT, CONTRACT_RENT_TEXT, PROPERTY_CEMIG_TEXT, PROPERTY_IPTU_TEXT, PERIOD_SECTION_TITLE, CONTRACT_PERIOD_TEXT, CONTRACT_START_DATE_TEXT, CONTRACT_END_DATE_TEXT, CONTRACT_PAYMENT_METHOD_TEXT, CONTRACTUAL_TERMS_SECTION_TITLE, CONTRACTUAL_TERMS_TEXT, CITY_AND_DATE_TEXT
  }

  public enum ContractCollections {
    SIGNING_TEXTS
  }

  public record TextBean(String text) {
    public String getText() {
      return text;
    }
  }
}
