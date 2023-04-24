package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newArray;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.saasquatch.jsonschemainferrer.annotations.Beta;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

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
   * @return An {@link ExamplesPolicy} that takes the first samples with a limit.
   */
  public static ExamplesPolicy useFirstSamples(@Nonnegative int limit) {
    return useFirstSamples(limit, type -> true);
  }

  /**
   * @param typePredicate The predicate for types. Note that the input of this predicate can be
   *        null.
   * @return An {@link ExamplesPolicy} that takes the first samples with a limit and a
   *         {@link Predicate} for types.
   */
  @Beta
  public static ExamplesPolicy useFirstSamples(@Nonnegative int limit,
      @Nonnull Predicate<String> typePredicate) {
    Objects.requireNonNull(typePredicate);
    //noinspection ConstantConditions
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
