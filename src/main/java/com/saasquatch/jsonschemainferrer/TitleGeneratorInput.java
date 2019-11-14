package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The input for {@link TitleGenerator}
 *
 * @author sli
 */
public interface TitleGeneratorInput {

  /**
   * @return The current field name, or null if it's not in an object.
   */
  @Nullable
  String getFieldName();

  @Nonnull
  SpecVersion getSpecVersion();

}
