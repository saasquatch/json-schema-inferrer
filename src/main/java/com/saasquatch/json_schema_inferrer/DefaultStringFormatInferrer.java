package com.saasquatch.json_schema_inferrer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * The default implementation fo {@link StringFormatInferrer}
 *
 * @author sli
 */
public enum DefaultStringFormatInferrer implements StringFormatInferrer {

  INSTANCE,;

  @Override
  public String infer(SpecVersion specVersion, String textValue) {
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
    if (specVersion == SpecVersion.DRAFT_07) {
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
    return null;
  }

}
