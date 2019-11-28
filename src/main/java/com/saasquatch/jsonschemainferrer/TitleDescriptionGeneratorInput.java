package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.saasquatch.jsonschemainferrer.annotations.NoExternalImpl;

/**
 * The input for {@link TitleDescriptionGenerator}
 *
 * @author sli
 */
@NoExternalImpl
public interface TitleDescriptionGeneratorInput {

  /**
   * @return The current field name, or null if it's not in an object.
   */
  @Nullable
  String getFieldName();

  @Nonnull
  SpecVersion getSpecVersion();

}
