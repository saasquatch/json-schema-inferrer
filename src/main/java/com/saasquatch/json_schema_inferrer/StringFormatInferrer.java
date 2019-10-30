package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for inferring the <a href=
 * "https://json-schema.org/understanding-json-schema/reference/string.html#format">format</a> of
 * strings
 *
 * @author sli
 */
@FunctionalInterface
public interface StringFormatInferrer {

  @Nullable
  String infer(@Nonnull SpecVersion specVersion, @Nonnull String textValue);

}
