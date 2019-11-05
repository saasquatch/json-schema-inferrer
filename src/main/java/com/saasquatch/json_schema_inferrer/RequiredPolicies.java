package com.saasquatch.json_schema_inferrer;

import static com.saasquatch.json_schema_inferrer.JunkDrawer.stringColToArrayDistinct;

/**
 * Utilities for {@link RequiredPolicy}
 *
 * @author sli
 */
public final class RequiredPolicies {

  /**
   * @return A singleton {@link RequiredPolicy} that does nothing
   */
  public static RequiredPolicy noOp() {
    return input -> null;
  }

  /**
   * @return A singleton {@link RequiredPolicy} that sets {@code required} to field names common to
   *         the given samples.
   */
  public static RequiredPolicy commonFieldNames() {
    return input -> stringColToArrayDistinct(input.getCommonFieldNames());
  }

}
