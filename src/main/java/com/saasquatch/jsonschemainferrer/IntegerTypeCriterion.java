package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;

/**
 * Criterion for deciding whether a number is an integer.
 *
 * @author sli
 */
public interface IntegerTypeCriterion {

  /**
   * @return Whether the given input is an integer (vs a number)
   */
  boolean isInteger(@Nonnull IntegerTypeCriterionInput input);

}
