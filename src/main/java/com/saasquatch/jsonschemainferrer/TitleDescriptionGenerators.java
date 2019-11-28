package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

  static TitleDescriptionGeneratorInput inputOf(@Nonnull ObjectNode schema,
      @Nullable String fieldName, @Nonnull SpecVersion specVersion) {
    return new TitleDescriptionGeneratorInput() {

      @Override
      public String getFieldName() {
        return fieldName;
      }

      @Override
      public SpecVersion getSpecVersion() {
        return specVersion;
      }

    };
  }

}
