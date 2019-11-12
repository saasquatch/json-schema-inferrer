package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Policy for {@code default}
 *
 * @author sli
 */
public interface DefaultPolicy {

  /**
   * Get the appropriate {@code default} from the given input.
   */
  @Nullable
  JsonNode getDefault(@Nonnull DefaultPolicyInput input);

}
