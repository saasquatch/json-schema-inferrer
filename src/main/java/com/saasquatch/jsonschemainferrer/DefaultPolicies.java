package com.saasquatch.jsonschemainferrer;

/**
 * Utilities for {@link DefaultPolicy}
 *
 * @author sli
 */
public final class DefaultPolicies {

  private DefaultPolicies() {}

  /**
   * @return a singleton {@link DefaultPolicy} that does nothing
   */
  public static DefaultPolicy noOp() {
    return input -> null;
  }

  /**
   * @return a singleton {@link DefaultPolicy} that always uses the first sample as {@code default}
   */
  public static DefaultPolicy useFirstSamples() {
    return DefaultPolicyInput::getFirstSample;
  }

  /**
   * @return a singleton {@link DefaultPolicy} that always uses the last sample as {@code default}
   */
  public static DefaultPolicy useLastSamples() {
    return DefaultPolicyInput::getLastSample;
  }

}
