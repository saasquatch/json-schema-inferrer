package com.saasquatch.jsonschemainferrer;

/**
 * Utilities for {@link DescriptionGenerator}s.
 *
 * @author sli
 */
public final class DescriptionGenerators {

  private DescriptionGenerators() {}

  /**
   * @return a singleton {@link DescriptionGenerator} that does not generate titles
   */
  public static DescriptionGenerator noOp() {
    return BuiltInDescriptionGenerators.NO_OP;
  }

  /**
   * @return a singleton {@link DescriptionGenerator} that uses field names as titles
   */
  public static DescriptionGenerator useFieldNamesAsTitles() {
    return BuiltInDescriptionGenerators.USE_FIELD_NAMES_AS_TITLES;
  }

}
