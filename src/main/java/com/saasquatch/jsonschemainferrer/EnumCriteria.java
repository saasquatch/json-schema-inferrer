package com.saasquatch.jsonschemainferrer;

/**
 * Utilities for {@link EnumCriterion}
 *
 * @author sli
 */
public final class EnumCriteria {

  private EnumCriteria() {}

  /**
   * @return A singleton {@link EnumCriterion} that always returns false
   */
  public static EnumCriterion noOp() {
    return input -> false;
  }

  /**
   * @return An {@link EnumCriterion} where the samples are part of an enum of the sample size is
   *         less than or equal to the limit.
   */
  public static EnumCriterion limit(int limit) {
    return input -> false;
  }

}
