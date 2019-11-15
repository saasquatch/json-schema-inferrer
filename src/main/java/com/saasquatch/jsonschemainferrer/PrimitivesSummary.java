package com.saasquatch.jsonschemainferrer;

import java.util.Collections;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Helper class for keeping track of examples
 *
 * @author sli
 */
final class PrimitivesSummary {

  private JsonNode firstSample;
  private JsonNode lastSample;
  private int minStringLength = -1;
  private int maxStringLength = -1;
  private final int examplesLimit;
  private final Set<JsonNode> examples;

  public PrimitivesSummary(int examplesLimit) {
    this.examplesLimit = examplesLimit;
    this.examples = examplesLimit > 0 ? new HashSet<>() : Collections.emptySet();
  }

  public void addSample(@Nonnull JsonNode sample) {
    if (examples.size() < examplesLimit) {
      examples.add(sample);
    }
    if (firstSample == null) {
      firstSample = sample;
    }
    lastSample = sample;
    final String textValue = sample.textValue();
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
  public Set<JsonNode> getExamples() {
    return examples;
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
