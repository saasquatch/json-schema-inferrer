package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utilities for {@link DefaultPolicy}
 *
 * @author sli
 */
public final class DefaultPolicies {

  private DefaultPolicies() {}

  /**
   * @return a singleton {@link DefaultPolicy} that does nothing
   */
  public static DefaultPolicy noOp() {
    return input -> null;
  }

  /**
   * @return a singleton {@link DefaultPolicy} that always uses the first sample as {@code default}
   */
  public static DefaultPolicy useFirstSamples() {
    return input -> {
      final Collection<? extends JsonNode> samples = input.getSamples();
      if (samples.isEmpty()) {
        return null;
      }
      return samples.iterator().next();
    };
  }

  /**
   * @return a singleton {@link DefaultPolicy} that always uses the last sample as {@code default}
   */
  public static DefaultPolicy useLastSamples() {
    return input -> {
      final Collection<? extends JsonNode> samples = input.getSamples();
      if (samples.isEmpty()) {
        return null;
      }
      return samples.stream().skip(samples.size() - 1).findFirst().orElse(null);
    };
  }

}
