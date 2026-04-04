package com.guilherme.emobiliaria.shared.pdf;

import java.util.Collection;
import java.util.EnumMap;

/**
 * Abstract class defining the structure for PDF templates, including template paths, parameters,
 * and collections.
 *
 * @param <P> the enum type for parameters
 * @param <C> the enum type for collections
 */
public abstract class PdfTemplate<P extends Enum<P>, C extends Enum<C>> {
  private final String templateName;

  protected PdfTemplate(String templateName) {
    this.templateName = templateName;
  }

  public String getTemplateName() {
    return templateName;
  }

  /**
   * Gets the name of the PDF template file.
   *
   * @return the template file name as a String
   */
  public abstract EnumMap<P, Object> getParameters();

  /**
   * Gets the collections to be included in the PDF.
   *
   * @return an EnumMap where keys are of type C and values are collections of objects
   */
  public abstract EnumMap<C, Collection<Object>> getCollections();

  public static String bold(String text) {
    return "<b>" + text + "</b>";
  }
}
