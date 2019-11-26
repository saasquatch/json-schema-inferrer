package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Policy for {@code default}
 *
 * @author sli
 */
@FunctionalInterface
public interface DefaultPolicy {

  /**
   * Get the appropriate {@code default} from the given input.
   */
  @Nullable
  JsonNode getDefault(@Nonnull DefaultPolicyInput input);

}
