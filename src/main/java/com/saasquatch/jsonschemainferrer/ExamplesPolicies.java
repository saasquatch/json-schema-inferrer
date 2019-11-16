package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newArray;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Utilities for {@link ExamplesPolicy}.
 *
 * @author sli
 */
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
    return first(limit, type -> true);
  }

  /**
   * @return An {@link ExamplesPolicy} that takes the first n samples and a {@link Predicate} for
   *         types.
   */
  public static ExamplesPolicy first(@Nonnegative int limit,
      @Nonnull Predicate<String> typePredicate) {
    Objects.requireNonNull(typePredicate);
    if (limit < 0) {
      throw new IllegalArgumentException("Invalid limit");
    }
    if (limit == 0) {
      return noOp();
    }
    return input -> {
      if (input.getSpecVersion().compareTo(SpecVersion.DRAFT_06) < 0) {
        return null;
      }
      if (!typePredicate.test(input.getType())) {
        return null;
      }
      final ArrayNode result = newArray();
      input.getSamples().stream().distinct().limit(limit).forEach(result::add);
      return result.size() == 0 ? null : result;
    };
  }

}
