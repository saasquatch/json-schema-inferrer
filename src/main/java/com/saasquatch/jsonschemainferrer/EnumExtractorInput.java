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
  private final String path;

  EnumExtractorInput(@Nonnull Collection<? extends JsonNode> samples,
      @Nonnull SpecVersion specVersion, @Nonnull String path) {
    this.samples = samples;
    this.specVersion = specVersion;
    this.path = path;
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
