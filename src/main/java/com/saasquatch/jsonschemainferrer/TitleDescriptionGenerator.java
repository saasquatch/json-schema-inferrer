package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for generating {@code title}s. Implementations are expected to be stateless and thread
 * safe.
 *
 * @author sli
 * @see TitleDescriptionGenerators
 */
@FunctionalInterface
public interface TitleDescriptionGenerator {

  /**
   * Generate a {@code title} based on the input
   *
   * @return The generated title, or null if no title is generated
   */
  @Nullable
  String generateTitle(@Nonnull TitleDescriptionGeneratorInput input);

  /**
   * Generate a {@code description} based on the input
   *
   * @return The generated title, or null if no description is generated
   */
  @Nullable
  default String generateDescription(@Nonnull TitleDescriptionGeneratorInput input) {
    return null;
  }

}
