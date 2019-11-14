package com.saasquatch.jsonschemainferrer;

/**
 * Utilities for {@link TitleGenerator}s.
 *
 * @author sli
 */
public final class TitleGenerators {

  private TitleGenerators() {}

  /**
   * @return a singleton {@link TitleGenerator} that does not generate titles
   */
  public static TitleGenerator noOp() {
    return input -> null;
  }

  /**
   * @return a singleton {@link TitleGenerator} that uses field names as titles
   */
  public static TitleGenerator useFieldNames() {
    return TitleGeneratorInput::getFieldName;
  }

}
