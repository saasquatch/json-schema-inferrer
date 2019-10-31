package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;

/**
 * The input for {@link StringFormatInferrer}
 *
 * @author sli
 */
public interface StringFormatInferrerInput {

  @Nonnull
  String getTextValue();

  @Nonnull
  SpecVersion getSpecVersion();

}
