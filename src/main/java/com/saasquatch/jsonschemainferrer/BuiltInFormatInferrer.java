package com.saasquatch.jsonschemainferrer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;

enum BuiltInFormatInferrer implements FormatInferrer {

  NO_OP {
    @Override
    public String inferFormat(FormatInferrerInput input) {
      return null;
    }
  },
  DATE_TIME {
    @Override
    public String inferFormat(FormatInferrerInput input) {
      final String textValue = input.getSample().textValue();
      if (textValue == null) {
        return null;
      }
      try {
        ZonedDateTime.parse(textValue);
        return "date-time";
      } catch (Exception e) {
        // Ignore
      }
      if (input.getSpecVersion().compareTo(SpecVersion.DRAFT_07) >= 0) {
        try {
          LocalDate.parse(textValue);
          return "date";
        } catch (Exception e) {
          // Ignore
        }
        try {
          LocalTime.parse(textValue);
          return "time";
        } catch (Exception e) {
          // Ignore
        }
        try {
          ZonedDateTime.parse("1111-11-11T" + textValue);
          return "time";
        } catch (Exception e) {
          // Ignore
        }
      }
      return null;
    }
  },
  EMAIL {
    @Override
    public String inferFormat(FormatInferrerInput input) {
      final String textValue = input.getSample().textValue();
      if (textValue == null) {
        return null;
      }
      if (EmailValidator.getInstance().isValid(textValue)) {
        return "email";
      }
      return null;
    }
  },
  IP {
    @Override
    public String inferFormat(FormatInferrerInput input) {
      final String textValue = input.getSample().textValue();
      if (textValue == null) {
        return null;
      }
      if (InetAddressValidator.getInstance().isValidInet4Address(textValue)) {
        return "ipv4";
      }
      if (InetAddressValidator.getInstance().isValidInet6Address(textValue)) {
        return "ipv6";
      }
      return null;
    }
  },;

}
