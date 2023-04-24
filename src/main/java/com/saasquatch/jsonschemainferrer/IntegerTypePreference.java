package com.saasquatch.jsonschemainferrer;

import com.saasquatch.jsonschemainferrer.annotations.Beta;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;

/**
 * Preference for whether the type {@code integer} should be used over {@code number} in the result
 * schema. Note that this class is not for determining whether a single number is an integer. That
 * is the job of the {@link IntegerTypeCriterion}.
 *
 * @author sli
 * @see IntegerTypeCriterion
 */
@Beta
public enum IntegerTypePreference {

  /**
   * Use {@code integer} if and only if all the samples are integers according to the
   * {@link IntegerTypeCriterion}. Use {@code number} otherwise.
   */
  IF_ALL {
    @Override
    boolean shouldUseInteger(@Nonnull BooleanSupplier currentNumberIsInteger,
        boolean allNumbersAreIntegers) {
      return allNumbersAreIntegers;
    }
  },

  /**
   * Use {@code integer} if an element is an integer according to the {@link IntegerTypeCriterion}.
   * Note that this option allows {@code integer} and {@code number} to coexist in one (sub)schema.
   */
  IF_ANY {
    @Override
    boolean shouldUseInteger(@Nonnull BooleanSupplier currentNumberIsInteger,
        boolean allNumbersAreIntegers) {
      return currentNumberIsInteger.getAsBoolean();
    }
  },

  /**
   * Never use {@code integer}. Always use {@code number} instead.
   */
  NEVER {
    @Override
    boolean shouldUseInteger(@Nonnull BooleanSupplier currentNumberIsInteger,
        boolean allNumbersAreIntegers) {
      return false;
    }
  },
  ;

  abstract boolean shouldUseInteger(@Nonnull BooleanSupplier currentNumberIsInteger,
      boolean allNumbersAreIntegers);

}
