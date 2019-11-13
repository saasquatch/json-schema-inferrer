package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Input for {@link DescriptionGenerator}
 *
 * @author sli
 */
public interface DescriptionGeneratorInput {

  /**
   * @return The current field name, or null if it's not in an object.
   */
  @Nullable
  String getFieldName();

  @Nonnull
  SpecVersion getSpecVersion();

}
