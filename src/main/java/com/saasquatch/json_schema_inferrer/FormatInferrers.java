package com.saasquatch.json_schema_inferrer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
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
        if (input.getSpecVersion().compareTo(SpecVersion.DRAFT_07) >= 0) {
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

  public static FormatInferrer email() {
    return CommonsValidatorFormatInferrer.EMAIL;
  }

  public static FormatInferrer ip() {
    return CommonsValidatorFormatInferrer.IP;
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
