package com.saasquatch.jsonschemainferrer;

import com.fasterxml.jackson.databind.JsonNode;
import com.saasquatch.jsonschemainferrer.annotations.Beta;
import java.util.Collection;
import javax.annotation.Nonnull;

/**
 * Input for {@link EnumExtractor}
 *
 * @author sli
 */
@Beta
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
