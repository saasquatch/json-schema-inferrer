package com.saasquatch.json_schema_inferrer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;

enum BuiltInFormatInferrer implements FormatInferrer {

  DATE_TIME {
    @Override
    public String inferFormat(FormatInferrerInput input) {
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
    }
  },
  EMAIL {
    @Override
    public String inferFormat(FormatInferrerInput input) {
      if (EmailValidator.getInstance().isValid(input.getSample().textValue())) {
        return "email";
      }
      return null;
    }
  },
  IP {
    @Override
    public String inferFormat(FormatInferrerInput input) {
      final String textValue = input.getSample().textValue();
      if (textValue != null) {
        if (InetAddressValidator.getInstance().isValidInet4Address(textValue)) {
          return "ipv4";
        }
        if (InetAddressValidator.getInstance().isValidInet6Address(textValue)) {
          return "ipv6";
        }
      }
      return null;
    }
  },;

}
