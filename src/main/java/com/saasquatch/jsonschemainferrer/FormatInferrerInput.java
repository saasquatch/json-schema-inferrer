package com.saasquatch.jsonschemainferrer;

import com.fasterxml.jackson.databind.JsonNode;
import javax.annotation.Nonnull;

/**
 * The input for {@link FormatInferrer}
 *
 * @author sli
 */
public final class FormatInferrerInput {

  private final JsonNode sample;
  private final SpecVersion specVersion;

  FormatInferrerInput(@Nonnull JsonNode sample, @Nonnull SpecVersion specVersion) {
    this.sample = sample;
    this.specVersion = specVersion;
  }

  /**
   * @return The input text value for inference
   */
  @Nonnull
  public JsonNode getSample() {
    return sample;
  }

  @Nonnull
  public SpecVersion getSpecVersion() {
    return specVersion;
  }

}
