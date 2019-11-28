package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.saasquatch.jsonschemainferrer.annotations.NoExternalImpl;

/**
 * Input for {@link RequiredPolicy}
 *
 * @author sli
 */
@NoExternalImpl
public interface RequiredPolicyInput {

  /**
   * @return The original samples
   */
  @Nonnull
  Collection<JsonNode> getSamples();

  @Nonnull
  SpecVersion getSpecVersion();

}
