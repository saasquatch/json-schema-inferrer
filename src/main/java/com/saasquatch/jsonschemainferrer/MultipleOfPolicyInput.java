package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.saasquatch.jsonschemainferrer.annotations.NoExternalImpl;

/**
 * Input for {@link MultipleOfPolicy}
 *
 * @author sli
 */
@NoExternalImpl
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
