package com.saasquatch.json_schema_inferrer;

/**
 * Preference for when type {@code integer} should be used over {@code number}.
 *
 * @author sli
 */
public enum IntegerTypePreference {

  /**
   * Use {@code integer} if all the samples are integral numbers. Use {@code number} otherwise.
   */
  IF_ALL,
  /**
   * Use {@code integer} if an element is an integral number. Note that this option allows
   * {@code integer} and {@code number} to coexist.
   */
  IF_ANY,
  /**
   * Never use {@code integer}. Always use {@code number}.
   */
  NEVER,;

}
