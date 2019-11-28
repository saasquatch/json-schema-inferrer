package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.saasquatch.jsonschemainferrer.annotations.NoExternalImpl;

/**
 * The input for {@link FormatInferrer}
 *
 * @author sli
 */
@NoExternalImpl
public interface FormatInferrerInput {

  /**
   * @return The input text value for inference
   */
  @Nonnull
  JsonNode getSample();

  @Nonnull
  SpecVersion getSpecVersion();

}
