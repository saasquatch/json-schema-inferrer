package com.saasquatch.jsonschemainferrer;

import java.util.Set;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface EnumExtractor {

  @Nonnull
  Set<Set<? extends JsonNode>> extractEnums(@Nonnull EnumExtractorInput input);

}
