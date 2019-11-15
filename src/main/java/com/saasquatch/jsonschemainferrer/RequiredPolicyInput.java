package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Input for {@link RequiredPolicy}
 *
 * @author sli
 */
public interface RequiredPolicyInput {

  /**
   * @return The original samples
   */
  @Nonnull
  Collection<JsonNode> getSamples();

  @Nonnull
  SpecVersion getSpecVersion();

}
