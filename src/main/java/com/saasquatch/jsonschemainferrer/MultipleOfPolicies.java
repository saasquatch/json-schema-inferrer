package com.saasquatch.jsonschemainferrer;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigInteger;

/**
 * Utilities for {@link MultipleOfPolicy}
 *
 * @author sli
 */
public final class MultipleOfPolicies {

  private MultipleOfPolicies() {
  }

  /**
   * @return a singleton {@link MultipleOfPolicy} that does nothing.
   */
  public static MultipleOfPolicy noOp() {
    return input -> null;
  }

  /**
   * @return a singleton {@link MultipleOfPolicy} that uses the GCD of number samples as
   * {@code multipleOf}.
   */
  public static MultipleOfPolicy gcd() {
    return input -> {
      // Only proceed if all numbers are integers
      final boolean allNumbersAreMathematicalIntegers = input.getSamples().stream()
          .filter(JsonNode::isNumber)
          .allMatch(JunkDrawer::isMathematicalIntegerNode);
      if (!allNumbersAreMathematicalIntegers) {
        return null;
      }
      return input.getSamples().stream()
          .filter(JsonNode::isNumber)
          .map(JsonNode::bigIntegerValue)
          .reduce(BigInteger::gcd)
          .filter(gcd -> BigInteger.ZERO.compareTo(gcd) != 0)
          .map(JunkDrawer::numberNode)
          .orElse(null);
    };
  }

}
