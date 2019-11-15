package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Policy for {@code examples}. Implementations are expected to be stateless and thread safe.
 *
 * @author sli
 */
@FunctionalInterface
public interface ExamplesPolicy {

  @Nullable
  JsonNode getExamples(@Nonnull ExamplesPolicyInput input);

}
