package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.node.ValueNode;

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
  ValueNode getValueNode();

  @Nonnull
  SpecVersion getSpecVersion();

}
