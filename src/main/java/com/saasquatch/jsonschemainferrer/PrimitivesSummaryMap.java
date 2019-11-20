package com.saasquatch.jsonschemainferrer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import com.fasterxml.jackson.databind.JsonNode;

@SuppressWarnings("serial") // We never serialize it
@NotThreadSafe
final class PrimitivesSummaryMap extends HashMap<List<String>, PrimitivesSummary> {

  public PrimitivesSummaryMap() {}

  public void addSample(@Nonnull String type, @Nullable String format, @Nonnull JsonNode sample) {
    final List<String> key = newKey(type, format);
    this.compute(key, (_key, primitiveSummary) -> {
      if (primitiveSummary == null) {
        primitiveSummary = new PrimitivesSummary();
      }
      primitiveSummary.addSample(sample);
      return primitiveSummary;
    });
  }

  public PrimitivesSummary getPrimitivesSummary(@Nonnull String type, @Nullable String format) {
    return this.get(newKey(type, format));
  }

  private static List<String> newKey(@Nonnull String type, @Nullable String format) {
    return Arrays.asList(Objects.requireNonNull(type), format);
  }

}
