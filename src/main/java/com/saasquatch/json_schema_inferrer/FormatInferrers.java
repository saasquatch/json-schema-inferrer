package com.saasquatch.json_schema_inferrer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;

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
   * The default implementation that supports a subset of the built-in formats.
   */
  static FormatInferrer defaultImpl() {
    return input -> {
      final String textValue = input.getJsonNode().textValue();
      if (textValue != null) {
        if (EmailValidator.getInstance().isValid(textValue)) {
          return "email";
        }
        if (InetAddressValidator.getInstance().isValidInet4Address(textValue)) {
          return "ipv4";
        }
        if (InetAddressValidator.getInstance().isValidInet6Address(textValue)) {
          return "ipv6";
        }
        if (UrlValidator.getInstance().isValid(textValue)) {
          return "uri";
        }
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

}
