package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The input for {@link FormatInferrer}
 *
 * @author sli
 */
public interface FormatInferrerInput {

  /**
   * @return The input text value for inference
   */
  @Nonnull
  JsonNode getJsonNode();

  @Nonnull
  SpecVersion getSpecVersion();

}
