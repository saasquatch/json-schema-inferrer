package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.isMathematicalInteger;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Criterion for deciding whether a number is an integer.
 *
 * @author sli
 */
public enum IntegerTypeCriterion {

  /**
   * A number is an integer if it's not a floating point number. For example, 1 is an integer and
   * 1.0 is not.
   */
  NON_FLOATING_POINT {
    @Override
    boolean isInteger(JsonNode numberNode) {
      return numberNode.isIntegralNumber();
    }
  },
  /**
   * A number is an integer if it's mathematical value is an integer. For example, 1 and 1.0 are
   * integers and 1.5 is not.
   */
  MATHEMATICAL_INTEGER {
    @Override
    boolean isInteger(JsonNode numberNode) {
      if (numberNode.isIntegralNumber()) {
        return true;
      } else if (numberNode.isFloat() || numberNode.isDouble()) {
        return isMathematicalInteger(numberNode.doubleValue());
      } else {
        return isMathematicalInteger(numberNode.decimalValue());
      }
    }
  },;

  abstract boolean isInteger(@Nonnull JsonNode numberNode);

}
