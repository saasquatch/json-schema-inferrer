package com.saasquatch.jsonschemainferrer;

import com.fasterxml.jackson.databind.JsonNode;
import com.saasquatch.jsonschemainferrer.annotations.Beta;
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

  FormatInferrerInput(@Nonnull JsonNode sample, @Nonnull SpecVersion specVersion,
      @Nonnull String path) {
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
   * This method is marked as {@link Beta @Beta} because it may not be perfect. The algorithm for
   * generating JSON path can be found at {@link JunkDrawer#escapeSingleQuoteString}.
   *
   * @return The JSON path of the current traversal.
   */
  @Beta
  @Nonnull
  public String getPath() {
    return path;
  }

}
