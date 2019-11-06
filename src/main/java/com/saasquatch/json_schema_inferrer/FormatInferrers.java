package com.saasquatch.json_schema_inferrer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Utilities for {@link FormatInferrer}s
 *
 * @author sli
 */
public final class FormatInferrers {

  /**
   * @return a singleton {@link FormatInferrer} that not infer formats.
   */
  public static FormatInferrer noOp() {
    return input -> null;
  }

  /**
   * The default implementation that infers date time formats.
   *
   * @return a singleton {@link FormatInferrer}
   */
  public static FormatInferrer dateTime() {
    return input -> {
      final String textValue = input.getSample().textValue();
      if (textValue != null) {
        try {
          ZonedDateTime.parse(textValue);
          return "date-time";
        } catch (Exception e) {
          // Ignore
        }
        if (input.getSpecVersion() == SpecVersion.DRAFT_07) {
          try {
            LocalTime.parse(textValue);
            return "time";
          } catch (Exception e) {
            // Ignore
          }
          try {
            LocalDate.parse(textValue);
            return "date";
          } catch (Exception e) {
            // Ignore
          }
        }
      }
      return null;
    };
  }

  /**
   * Convenience method for {@link #chained(Collection)}
   */
  public static FormatInferrer chained(@Nonnull FormatInferrer... formatInferrers) {
    // Default to the other method to create a defensive copy on purpose
    return chained(Arrays.asList(formatInferrers));
  }

  /**
   * @return A {@link FormatInferrer} that uses the given {@link FormatInferrer}s in the original
   *         order, and uses the first non-null result available.
   */
  public static FormatInferrer chained(@Nonnull Collection<FormatInferrer> formatInferrers) {
    for (FormatInferrer formatInferrer : formatInferrers) {
      Objects.requireNonNull(formatInferrer);
    }
    switch (formatInferrers.size()) {
      case 0:
        throw new IllegalArgumentException("Empty formatInferrers");
      case 1:
        return formatInferrers.iterator().next();
      default:
        break;
    }
    // Create a defensive copy on purpose
    return new ChainedFormatInferrer(formatInferrers.toArray(new FormatInferrer[0]));
  }

}
