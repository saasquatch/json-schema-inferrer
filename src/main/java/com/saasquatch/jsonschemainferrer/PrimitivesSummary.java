package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.getSerializedTextValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.OptionalInt;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

  private JsonNode firstSample;
  private JsonNode lastSample;
  private int minStringLength = -1;
  private int maxStringLength = -1;
  private final Collection<JsonNode> samples;

  public PrimitivesSummary() {
    this.samples = new ArrayList<>();
  }

  public void addSample(@Nonnull JsonNode sample) {
    samples.add(sample);
    if (firstSample == null) {
      firstSample = sample;
    }
    lastSample = sample;
    final String textValue = getSerializedTextValue(sample);
    if (textValue != null) {
      // DO NOT use String.length()
      final int stringLength = textValue.codePointCount(0, textValue.length());
      if (minStringLength < 0 || stringLength < minStringLength) {
        minStringLength = stringLength;
      }
      if (maxStringLength < 0 || stringLength > maxStringLength) {
        maxStringLength = stringLength;
      }
    }
  }

  @Nonnull
  public Collection<JsonNode> getSamples() {
    return Collections.unmodifiableCollection(samples);
  }

  @Nullable
  public JsonNode getFirstSample() {
    return firstSample;
  }

  @Nullable
  public JsonNode getLastSample() {
    return lastSample;
  }

  @Nonnull
  public OptionalInt getMinStringLength() {
    return minStringLength < 0 ? OptionalInt.empty() : OptionalInt.of(minStringLength);
  }

  @Nonnull
  public OptionalInt getMaxStringLength() {
    return maxStringLength < 0 ? OptionalInt.empty() : OptionalInt.of(maxStringLength);
  }

}
