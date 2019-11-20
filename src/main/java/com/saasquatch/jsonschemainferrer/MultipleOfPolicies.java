package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.allNumbersAreIntegers;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.numberNode;
import java.math.BigInteger;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utilities for {@link MultipleOfPolicy}
 *
 * @author sli
 */
public final class MultipleOfPolicies {

  private MultipleOfPolicies() {}

  /**
   * @return a singleton {@link MultipleOfPolicy} that does nothing.
   */
  public static MultipleOfPolicy noOp() {
    return input -> null;
  }

  /**
   * @return a singleton {@link MultipleOfPolicy} that uses the GCD of number samples as
   *         {@code multipleOf}.
   */
  public static MultipleOfPolicy gcd() {
    return input -> {
      // Only proceed if all numbers are integers
      if (!Consts.Types.INTEGER.equals(input.getType())) {
        if (!allNumbersAreIntegers(input.getSamples())) {
          return null;
        }
      }
      return input.getSamples().stream()
          .filter(JsonNode::isNumber)
          .map(JsonNode::bigIntegerValue)
          .reduce(BigInteger::gcd)
          .filter(gcd -> BigInteger.ZERO.compareTo(gcd) != 0)
          .map(gcd -> numberNode(gcd))
          .orElse(null);
    };
  }

}
