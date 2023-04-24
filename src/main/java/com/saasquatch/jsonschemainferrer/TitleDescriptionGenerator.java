package com.saasquatch.jsonschemainferrer;

import com.saasquatch.jsonschemainferrer.annotations.Beta;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for generating {@code title}s, {@code description}s, and related things.
 * Implementations are expected to be stateless and thread safe.
 *
 * @author sli
 * @see TitleDescriptionGenerators
 */
@Beta
public interface TitleDescriptionGenerator {

  /**
   * Generate a {@code title} based on the input
   *
   * @return The generated title, or null if no title is generated
   */
  @Nullable
  default String generateTitle(@Nonnull TitleDescriptionGeneratorInput input) {
    return null;
  }

  /**
   * Generate a {@code description} based on the input
   *
   * @return The generated title, or null if no description is generated
   */
  @Nullable
  default String generateDescription(@Nonnull TitleDescriptionGeneratorInput input) {
    return null;
  }

  /**
   * Generate a {@code $comment} based on the input. Note that {@code $comment} is new in draft-07,
   * and it's the implementations' job to be compliant with the specs.
   *
   * @return The generated comment, or null if no comment is generated
   */
  @Nullable
  default String generateComment(@Nonnull TitleDescriptionGeneratorInput input) {
    return null;
  }

}
