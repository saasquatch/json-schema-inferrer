package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Input for {@link EnumCriterion}
 *
 * @author sli
 */
public final class EnumCriterionInput {

  private final Collection<? extends JsonNode> samples;
  private final SpecVersion specVersion;

  EnumCriterionInput(@Nonnull Collection<? extends JsonNode> samples,
      @Nonnull SpecVersion specVersion) {
    this.samples = samples;
    this.specVersion = specVersion;
  }

  @Nonnull
  public Collection<? extends JsonNode> getSamples() {
    return samples;
  }

  @Nonnull
  public SpecVersion getSpecVersion() {
    return specVersion;
  }

}
