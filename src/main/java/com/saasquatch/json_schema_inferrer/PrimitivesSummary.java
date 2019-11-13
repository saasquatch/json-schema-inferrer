package com.saasquatch.json_schema_inferrer;

import java.util.Collections;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import javax.annotation.Nonnull;
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

  public void addExample(@Nonnull JsonNode example) {
    if (examples.size() < examplesLimit) {
      examples.add(example);
    }
    if (firstSample == null) {
      firstSample = example;
    }
    lastSample = example;
    final String textValue = example.textValue();
    if (textValue != null) {
      final int stringLength = textValue.length();
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

  public JsonNode getFirstSample() {
    return firstSample;
  }

  public JsonNode getLastSample() {
    return lastSample;
  }

  public OptionalInt getMinStringLength() {
    return minStringLength < 0 ? OptionalInt.empty() : OptionalInt.of(minStringLength);
  }

  public OptionalInt getMaxStringLength() {
    return maxStringLength < 0 ? OptionalInt.empty() : OptionalInt.of(maxStringLength);
  }

}
