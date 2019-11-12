package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Input for {@link DefaultPolicy}
 *
 * @author sli
 */
public interface DefaultPolicyInput {

  @Nullable
  JsonNode getFirstSample();

  @Nullable
  JsonNode getLastSample();

  @Nonnull
  SpecVersion getSpecVersion();

}
