package com.saasquatch.json_schema_inferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Input for {@link RequiredPolicy}
 *
 * @author sli
 */
public interface RequiredPolicyInput {

  /**
   * @return The input JSON schema
   */
  @Nonnull
  ObjectNode getSchema();

  /**
   * @return The original samples
   */
  @Nonnull
  Collection<JsonNode> getSamples();

  @Nonnull
  SpecVersion getSpecVersion();

}
