package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;
import com.saasquatch.jsonschemainferrer.annotations.NoExternalImpl;

/**
 * Input for {@link DefaultPolicy}
 *
 * @author sli
 */
@NoExternalImpl
public interface DefaultPolicyInput {

  /**
   * @return The first sample for the current inferred schema, if available.
   */
  @Nullable
  JsonNode getFirstSample();

  /**
   * @return The last sample for the current inferred schema, if available.
   */
  @Nullable
  JsonNode getLastSample();

  @Nonnull
  SpecVersion getSpecVersion();

}
