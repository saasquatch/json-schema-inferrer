package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Policy for {@code additionalProperties}. Implementations are expected to be stateless and thread
 * safe.
 *
 * @author sli
 * @see AdditionalPropertiesPolicies
 */
@FunctionalInterface
public interface AdditionalPropertiesPolicy {

  /**
   * Get the appropriate {@code additionalProperties} field based on the input. Note that this
   * method should not modify the original input.
   */
  @Nullable
  JsonNode getAdditionalProperties(@Nonnull AdditionalPropertiesPolicyInput input);

}
