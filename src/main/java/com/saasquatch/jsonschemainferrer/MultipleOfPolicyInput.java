package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Input for {@link MultipleOfPolicy}
 *
 * @author sli
 */
public interface MultipleOfPolicyInput {

  /**
   * @return The number samples
   */
  @Nonnull
  Collection<JsonNode> getSamples();

  @Nonnull
  String getType();

  @Nonnull
  SpecVersion getSpecVersion();

}
