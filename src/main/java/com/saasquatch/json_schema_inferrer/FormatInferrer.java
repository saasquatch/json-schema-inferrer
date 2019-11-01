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

  @Nullable
  String infer(@Nonnull FormatInferrerInput input);

  public static FormatInferrer noOp() {
    return NoOpStringFormatInferrer.INSTANCE;
  }

}
