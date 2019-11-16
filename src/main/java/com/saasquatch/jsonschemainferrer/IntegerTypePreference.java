package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Preference for when type {@code integer} should be used over {@code number}.
 *
 * @author sli
 */
public enum IntegerTypePreference {

  /**
   * Use {@code integer} if and only if all the samples are integral numbers. Use {@code number}
   * otherwise.
   */
  IF_ALL {
    @Override
    boolean shouldUseInteger(JsonNode sample, boolean allNumbersAreIntegers) {
      return allNumbersAreIntegers;
    }
  },
  /**
   * Use {@code integer} if an element is an integral number. Note that this option allows
   * {@code integer} and {@code number} to coexist.
   */
  IF_ANY {
    @Override
    boolean shouldUseInteger(JsonNode sample, boolean allNumbersAreIntegers) {
      return sample.isIntegralNumber();
    }
  },
  /**
   * Never use {@code integer}. Always use {@code number} instead.
   */
  NEVER {
    @Override
    boolean shouldUseInteger(JsonNode sample, boolean allNumbersAreIntegers) {
      return false;
    }
  },;

  abstract boolean shouldUseInteger(@Nonnull JsonNode sample, boolean allNumbersAreIntegers);

}