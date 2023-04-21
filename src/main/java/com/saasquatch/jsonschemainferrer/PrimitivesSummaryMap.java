package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.entryOf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link Map} type to keep track of {@link PrimitivesSummary}. The keys are combinations of
 * {@code type} and {@code format}.
 *
 * @author sli
 */
@NotThreadSafe
final class PrimitivesSummaryMap extends HashMap<Map.Entry<String, String>, PrimitivesSummary> {

  public PrimitivesSummaryMap() {}

  /**
   * Keep track of a new sample
   *
   * @param sample the new sample
   */
  public void addSample(@Nonnull String type, @Nullable String format, @Nonnull JsonNode sample) {
    final Map.Entry<String, String> key = newKey(type, format);
    this.compute(key, (_key, primitivesSummary) -> {
      if (primitivesSummary == null) {
        primitivesSummary = new PrimitivesSummary();
      }
      primitivesSummary.addSample(sample);
      return primitivesSummary;
    });
  }

  /**
   * Retrieve the {@link PrimitivesSummary} for the given type/format combination. Technically this
   * method can return null, but it should not return null for types we've tracked.
   */
  @Nullable
  public PrimitivesSummary getPrimitivesSummary(@Nonnull String type, @Nullable String format) {
    return this.get(newKey(type, format));
  }

  private static Map.Entry<String, String> newKey(@Nonnull String type, @Nullable String format) {
    return entryOf(Objects.requireNonNull(type), format);
  }

}
