package com.saasquatch.jsonschemainferrer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * Built-in singleton implementations of {@link FormatInferrer}. Not public. Exists only to make
 * {@link FormatInferrers} a little bit cleaner.
 *
 * @author sli
 * @see FormatInferrers
 */
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
        return Consts.Formats.DATE_TIME;
      } catch (Exception e) {
        // Ignore
      }
      if (input.getSpecVersion().compareTo(SpecVersion.DRAFT_07) >= 0) {
        try {
          LocalDate.parse(textValue);
          return Consts.Formats.DATE;
        } catch (Exception e) {
          // Ignore
        }
        try {
          // This only covers time strings without time zones
          LocalTime.parse(textValue);
          return Consts.Formats.TIME;
        } catch (Exception e) {
          // Ignore
        }
        try {
          // This covers time strings with time zones
          OffsetTime.parse(textValue);
          return Consts.Formats.TIME;
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
        return Consts.Formats.EMAIL;
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
        return Consts.Formats.IPV4;
      }
      if (InetAddressValidator.getInstance().isValidInet6Address(textValue)) {
        return Consts.Formats.IPV6;
      }
      return null;
    }
  },;

}
