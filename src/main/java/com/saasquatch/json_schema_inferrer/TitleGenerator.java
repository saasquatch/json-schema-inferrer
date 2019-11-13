package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for generating {@code title}s based on the sample JSON
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
