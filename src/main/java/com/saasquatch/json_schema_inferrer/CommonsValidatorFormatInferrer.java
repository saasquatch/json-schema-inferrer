package com.saasquatch.json_schema_inferrer;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;

enum CommonsValidatorFormatInferrer implements FormatInferrer {

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
