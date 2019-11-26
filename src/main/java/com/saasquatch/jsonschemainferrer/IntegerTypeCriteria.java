package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.isMathematicalIntegerNode;

/**
 * Utilities for {@link IntegerTypeCriterion}
 *
 * @author sli
 */
public final class IntegerTypeCriteria {

  private IntegerTypeCriteria() {}

  /**
   * A number is an integer if it's not a floating point number or a number with a decimal point.
   * For example, 1 is an integer and 1.0 is not.
   */
  public static IntegerTypeCriterion nonFloatingPoint() {
    return input -> input.getSample().isIntegralNumber();
  }

  /**
   * A number is an integer if its mathematical value is an integer. For example, 1 and 1.0 are
   * integers and 1.5 is not.
   */
  public static IntegerTypeCriterion mathematicalInteger() {
    return input -> isMathematicalIntegerNode(input.getSample());
  }

}
