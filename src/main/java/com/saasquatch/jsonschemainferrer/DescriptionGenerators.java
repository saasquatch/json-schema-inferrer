package com.saasquatch.jsonschemainferrer;

/**
 * Utilities for {@link DescriptionGenerator}
 *
 * @author sli
 */
public final class DescriptionGenerators {

  private DescriptionGenerators() {}

  /**
   * @return a singleton {@link DescriptionGenerator} that does nothing
   */
  public static DescriptionGenerator noOp() {
    return input -> null;
  }

}
