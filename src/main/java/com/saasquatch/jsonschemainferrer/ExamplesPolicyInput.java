package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Input for {@link ExamplesPolicy}
 *
 * @author sli
 */
public interface ExamplesPolicyInput {

  /**
   * @return The samples for the current type
   */
  @Nonnull
  Collection<JsonNode> getSamples();

  @Nonnull
  String getType();

  @Nonnull
  SpecVersion getSpecVersion();

}
