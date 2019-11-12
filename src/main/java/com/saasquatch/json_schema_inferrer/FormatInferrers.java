package com.saasquatch.json_schema_inferrer;

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Utilities for {@link FormatInferrer}s
 *
 * @author sli
 */
public final class FormatInferrers {

  /**
   * @return a singleton {@link FormatInferrer} that does not infer formats.
   */
  public static FormatInferrer noOp() {
    return input -> null;
  }

  /**
   * @return a singleton {@link FormatInferrer} that infers date time formats.
   */
  public static FormatInferrer dateTime() {
    return BuiltInFormatInferrer.DATE_TIME;
  }

  /**
   * Note that this {@link FormatInferrer} requires commons-validator dependency.
   *
   * @return a singleton {@link FormatInferrer} that infers email formats.
   */
  public static FormatInferrer email() {
    return BuiltInFormatInferrer.EMAIL;
  }

  /**
   * Note that this {@link FormatInferrer} requires commons-validator dependency.
   *
   * @return a singleton {@link FormatInferrer} that infers IP formats.
   */
  public static FormatInferrer ip() {
    return BuiltInFormatInferrer.IP;
  }

  /**
   * @return A {@link FormatInferrer} that uses the given {@link FormatInferrer}s in the original
   *         order, and uses the first non-null result available.
   * @throws NullPointerException if the input has null elements
   */
  public static FormatInferrer chained(@Nonnull FormatInferrer... formatInferrers) {
    return _chained(formatInferrers.clone());
  }

  private static FormatInferrer _chained(@Nonnull FormatInferrer[] formatInferrers) {
    for (FormatInferrer formatInferrer : formatInferrers) {
      Objects.requireNonNull(formatInferrer);
    }
    switch (formatInferrers.length) {
      case 0:
        return noOp();
      case 1:
        return formatInferrers[0];
      default:
        break;
    }
    return input -> {
      for (FormatInferrer formatInferrer : formatInferrers) {
        final String result = formatInferrer.inferFormat(input);
        if (result != null) {
          return result;
        }
      }
      return null;
    };
  }

}
