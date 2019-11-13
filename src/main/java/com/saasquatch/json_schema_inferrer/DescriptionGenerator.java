package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for generating {@code description}s based on the sample JSON
 *
 * @author sli
 * @see DescriptionGenerators
 */
@FunctionalInterface
public interface DescriptionGenerator {

  /**
   * Generate a {@code description} based on the input
   *
   * @return The generated description, or null if no description is generated
   */
  @Nullable
  String generateDescription(@Nonnull DescriptionGeneratorInput input);

}
