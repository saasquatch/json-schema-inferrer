package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnegative;
import com.fasterxml.jackson.databind.node.ArrayNode;

public final class ExamplesPolicies {

  private ExamplesPolicies() {}

  /**
   * @return A singleton {@link ExamplesPolicy} that does nothing.
   */
  public static ExamplesPolicy noOp() {
    return input -> null;
  }

  /**
   * @return An {@link ExamplesPolicy} that takes the first n samples.
   */
  public static ExamplesPolicy first(@Nonnegative int limit) {
    if (limit == 0) {
      return noOp();
    } else if (limit < 0) {
      throw new IllegalArgumentException("Invalid limit");
    }
    return input -> {
      if (input.getSpecVersion().compareTo(SpecVersion.DRAFT_06) < 0) {
        return null;
      }
      final ArrayNode result = JunkDrawer.newArray();
      input.getSamples().stream().distinct().limit(limit).forEach(result::add);
      return result.size() == 0 ? null : result;
    };
  }

}
