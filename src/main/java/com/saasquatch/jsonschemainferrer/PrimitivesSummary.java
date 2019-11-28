package com.saasquatch.jsonschemainferrer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Helper class for keeping track of primitive samples
 *
 * @author sli
 * @see PrimitivesSummaryMap
 */
@NotThreadSafe
final class PrimitivesSummary {

  private final Collection<JsonNode> samples;

  public PrimitivesSummary() {
    this.samples = new ArrayList<>();
  }

  public void addSample(@Nonnull JsonNode sample) {
    samples.add(sample);
  }

  @Nonnull
  public Collection<JsonNode> getSamples() {
    return Collections.unmodifiableCollection(samples);
  }

}
