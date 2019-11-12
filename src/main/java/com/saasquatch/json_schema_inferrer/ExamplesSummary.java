package com.saasquatch.json_schema_inferrer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Helper class for keeping track of examples
 *
 * @author sli
 */
final class ExamplesSummary {

  private JsonNode firstSample;
  private JsonNode lastSample;
  private final int examplesLimit;
  private final Set<JsonNode> examples;

  public ExamplesSummary(int examplesLimit) {
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
  }

  public JsonNode getFirstSample() {
    return firstSample;
  }

  public JsonNode getLastSample() {
    return lastSample;
  }

  @Nonnull
  public Set<JsonNode> getExamples() {
    return examples;
  }

}
