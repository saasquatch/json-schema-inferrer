package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;

/**
 * The input for {@link TitleGenerator}
 *
 * @author sli
 */
public interface TitleGeneratorInput {

  @Nonnull
  String getFieldName();

  @Nonnull
  SpecVersion getSpecVersion();

}
