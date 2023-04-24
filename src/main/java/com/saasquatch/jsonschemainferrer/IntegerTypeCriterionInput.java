package com.saasquatch.jsonschemainferrer;

import com.fasterxml.jackson.databind.JsonNode;
import javax.annotation.Nonnull;

/**
 * Input for {@link IntegerTypeCriterion}
 *
 * @author sli
 */
public final class IntegerTypeCriterionInput {

  private final JsonNode sample;
  private final SpecVersion specVersion;

  IntegerTypeCriterionInput(@Nonnull JsonNode sample, @Nonnull SpecVersion specVersion) {
    this.sample = sample;
    this.specVersion = specVersion;
  }

  @Nonnull
  public JsonNode getSample() {
    return sample;
  }

  @Nonnull
  public SpecVersion getSpecVersion() {
    return specVersion;
  }

}
