package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface for extracting {@code enum} groups from samples.
 *
 * @author sli
 * @see EnumExtractors
 * @see EnumExtractorInput
 */
@FunctionalInterface
public interface EnumExtractor {

  /**
   * @return The <em>group</em> of enums. Note that each group is expected to be not null and not
   *         empty. All the elements in each group are expected to come directly from the given
   *         samples if possible to ensure {@link JsonNode#equals(Object)} works correctly.
   */
  @Nonnull
  Collection<Collection<? extends JsonNode>> extractEnums(@Nonnull EnumExtractorInput input);

}
