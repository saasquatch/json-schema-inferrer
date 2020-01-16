package com.saasquatch.jsonschemainferrer;

import com.saasquatch.jsonschemainferrer.annotations.Beta;

/**
 * Utilities for {@link TitleDescriptionGenerator}s.
 *
 * @author sli
 */
@Beta
public final class TitleDescriptionGenerators {

  private TitleDescriptionGenerators() {}

  /**
   * @return a singleton {@link TitleDescriptionGenerator} that does not generate titles
   */
  public static TitleDescriptionGenerator noOp() {
    return BuiltInTitleDescriptionGenerators.NO_OP;
  }

  /**
   * @return a singleton {@link TitleDescriptionGenerator} that uses field names as titles
   */
  public static TitleDescriptionGenerator useFieldNamesAsTitles() {
    return BuiltInTitleDescriptionGenerators.USE_FIELD_NAMES_AS_TITLES;
  }

}
