package com.saasquatch.json_schema_inferrer;

import java.util.Set;
import javax.annotation.Nonnull;
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

  @Nonnull
  Set<String> getCommonFieldNames();

  @Nonnull
  SpecVersion getSpecVersion();

}
