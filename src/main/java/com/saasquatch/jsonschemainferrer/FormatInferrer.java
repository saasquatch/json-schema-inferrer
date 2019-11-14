package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for inferring the <a href=
 * "https://json-schema.org/understanding-json-schema/reference/string.html#format">format</a> of
 * strings
 *
 * @author sli
 * @see FormatInferrers
 */
@FunctionalInterface
public interface FormatInferrer {

  /**
   * Infer the {@code format} based on the input.
   *
   * @return the inferred format, or null if no format is inferred
   */
  @Nullable
  String inferFormat(@Nonnull FormatInferrerInput input);

}
