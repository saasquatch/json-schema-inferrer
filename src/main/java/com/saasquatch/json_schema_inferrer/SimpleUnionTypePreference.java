package com.saasquatch.json_schema_inferrer;

/**
 * The preference for situations where an element can be multiple "simple" types.
 *
 * @author sli
 */
public enum SimpleUnionTypePreference {

  /**
   * {@code type} as an array is preferred when possible.
   */
  TYPE_AS_ARRAY,
  /**
   * Always use {@code anyOf}.
   */
  ANY_OF,;

}
