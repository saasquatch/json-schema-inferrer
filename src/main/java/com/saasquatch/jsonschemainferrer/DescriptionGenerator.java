package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for generating {@code description}s. Implementations are expected to be stateless and
 * thread safe.
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
