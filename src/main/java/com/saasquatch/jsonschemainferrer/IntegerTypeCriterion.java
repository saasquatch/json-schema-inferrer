package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;

/**
 * Criterion for deciding whether a number is an integer. Implementations are expected to be
 * stateless and thread safe. Note that this class does not necessarily decide whether the type
 * {@code integer} should be used over {@code number}. That is the job of the
 * {@link IntegerTypePreference}.
 *
 * @author sli
 * @see IntegerTypeCriteria
 * @see IntegerTypePreference
 */
@FunctionalInterface
public interface IntegerTypeCriterion {

  /**
   * @return Whether the given input is an integer (vs a number)
   */
  boolean isInteger(@Nonnull IntegerTypeCriterionInput input);

}
