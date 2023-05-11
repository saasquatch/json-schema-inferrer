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
  private final String path;

  FormatInferrerInput(@Nonnull JsonNode sample, @Nonnull SpecVersion specVersion, @Nonnull String path) {
    this.sample = sample;
    this.specVersion = specVersion;
    this.path = path;
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

  /**
   * @return The json path of the current traversal.
   */
  @Nonnull
  public String getPath() {
    return path;
  }

}
