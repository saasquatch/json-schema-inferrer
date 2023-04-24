package com.saasquatch.jsonschemainferrer;

import com.saasquatch.jsonschemainferrer.annotations.Beta;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The input for {@link TitleDescriptionGenerator}
 *
 * @author sli
 */
@Beta
public final class TitleDescriptionGeneratorInput {

  private final String fieldName;
  private final SpecVersion specVersion;

  TitleDescriptionGeneratorInput(@Nullable String fieldName, @Nonnull SpecVersion specVersion) {
    this.fieldName = fieldName;
    this.specVersion = specVersion;
  }

  /**
   * @return The current field name, or null if it's not in an object.
   */
  @Nullable
  public String getFieldName() {
    return fieldName;
  }

  @Nonnull
  public SpecVersion getSpecVersion() {
    return specVersion;
  }

}
