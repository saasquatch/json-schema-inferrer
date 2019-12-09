package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

public final class EnumExtractorInput {

  private final Collection<? extends JsonNode> samples;
  private final SpecVersion specVersion;

  EnumExtractorInput(@Nonnull Collection<? extends JsonNode> samples,
      @Nonnull SpecVersion specVersion) {
    this.samples = samples;
    this.specVersion = specVersion;
  }

  public Collection<? extends JsonNode> getSamples() {
    return samples;
  }

  public SpecVersion getSpecVersion() {
    return specVersion;
  }

}
