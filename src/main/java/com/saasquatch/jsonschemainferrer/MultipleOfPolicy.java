package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Policy for {@code multipleOf}.
 *
 * @author sli
 * @see MultipleOfPolicies
 */
@FunctionalInterface
public interface MultipleOfPolicy {

  @Nullable
  JsonNode getMultipleOf(@Nonnull MultipleOfPolicyInput input);

}
