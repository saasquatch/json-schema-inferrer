package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Policy for {@code required}
 *
 * @author sli
 * @see RequiredPolicies
 */
@FunctionalInterface
public interface RequiredPolicy {

  /**
   * Get the appropriate {@code required} field for the input. Note that this method should not
   * modify the input.
   */
  JsonNode getRequired(@Nonnull RequiredPolicyInput input);

}
