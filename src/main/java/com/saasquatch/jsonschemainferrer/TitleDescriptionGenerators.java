package com.saasquatch.jsonschemainferrer;

/**
 * Utilities for {@link TitleDescriptionGenerator}s.
 *
 * @author sli
 */
public final class TitleDescriptionGenerators {

  private TitleDescriptionGenerators() {}

  /**
   * @return a singleton {@link TitleDescriptionGenerator} that does not generate titles
   */
  public static TitleDescriptionGenerator noOp() {
    return input -> null;
  }

  /**
   * @return a singleton {@link TitleDescriptionGenerator} that uses field names as titles
   */
  public static TitleDescriptionGenerator useFieldNamesAsTitles() {
    return TitleDescriptionGeneratorInput::getFieldName;
  }

}
