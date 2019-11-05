package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for inferring the <a href=
 * "https://json-schema.org/understanding-json-schema/reference/string.html#format">format</a> of
 * strings
 *
 * @author sli
 * @see #noOp()
 */
@FunctionalInterface
public interface FormatInferrer {

  /**
   * Infer the {@code format} based on the input.
   *
   * @return the inferred format, or null if no format is inferred
   */
  @Nullable
  String infer(@Nonnull FormatInferrerInput input);

  /**
   * @return A singleton no-op {@link FormatInferrer} that always returns null
   */
  public static FormatInferrer noOp() {
    return input -> null;
  }

  /**
   * @return A singleton {@link FormatInferrer} with the default implementation
   */
  public static FormatInferrer defaultImpl() {
    return DefaultFormatInferrer.INSTANCE;
  }

}
