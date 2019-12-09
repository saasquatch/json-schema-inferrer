package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Input for {@link EnumExtractor}
 *
 * @author sli
 */
public final class EnumExtractorInput {

  private final Collection<? extends JsonNode> samples;
  private final SpecVersion specVersion;

  EnumExtractorInput(@Nonnull Collection<? extends JsonNode> samples,
      @Nonnull SpecVersion specVersion) {
    this.samples = samples;
    this.specVersion = specVersion;
  }

  /**
   * @return The current samples
   */
  @Nonnull
  public Collection<? extends JsonNode> getSamples() {
    return samples;
  }

  /**
   * @return The current {@link SpecVersion}
   */
  @Nonnull
  public SpecVersion getSpecVersion() {
    return specVersion;
  }

}
