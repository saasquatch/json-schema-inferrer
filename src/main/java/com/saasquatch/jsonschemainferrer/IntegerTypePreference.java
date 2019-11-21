package com.saasquatch.jsonschemainferrer;

/**
 * Preference for when type {@code integer} should be used over {@code number} in the result schema.
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
    boolean shouldUseInteger(boolean currentNumberIsInteger, boolean allNumbersAreIntegers) {
      return allNumbersAreIntegers;
    }
  },
  /**
   * Use {@code integer} if an element is an integral number. Note that this option allows
   * {@code integer} and {@code number} to coexist in one (sub)schema.
   */
  IF_ANY {
    @Override
    boolean shouldUseInteger(boolean currentNumberIsInteger, boolean allNumbersAreIntegers) {
      return currentNumberIsInteger;
    }
  },
  /**
   * Never use {@code integer}. Always use {@code number} instead.
   */
  NEVER {
    @Override
    boolean shouldUseInteger(boolean currentNumberIsInteger, boolean allNumbersAreIntegers) {
      return false;
    }
  },;

  abstract boolean shouldUseInteger(boolean currentNumberIsInteger, boolean allNumbersAreIntegers);

}
