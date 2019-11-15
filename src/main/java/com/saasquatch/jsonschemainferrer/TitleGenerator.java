package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for generating {@code title}s. Implementations are expected to be stateless and thread
 * safe.
 *
 * @author sli
 * @see TitleGenerators
 */
@FunctionalInterface
public interface TitleGenerator {

  /**
   * Generate a {@code title} based on the input
   *
   * @return The generated title, or null if no title is generated
   */
  @Nullable
  String generateTitle(@Nonnull TitleGeneratorInput input);

}
