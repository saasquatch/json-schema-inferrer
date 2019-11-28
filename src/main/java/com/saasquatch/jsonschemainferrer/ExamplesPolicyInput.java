package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.saasquatch.jsonschemainferrer.annotations.NoExternalImpl;

/**
 * Input for {@link ExamplesPolicy}
 *
 * @author sli
 */
@NoExternalImpl
public interface ExamplesPolicyInput {

  /**
   * @return The samples for the current type
   */
  @Nonnull
  Collection<JsonNode> getSamples();

  /**
   * @return The {@code type} for the current inferred schema
   */
  @Nonnull
  String getType();

  @Nonnull
  SpecVersion getSpecVersion();

}
