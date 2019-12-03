package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.unmodifiableSetOf;
import java.util.Collections;
import java.util.Set;

/**
 * Constants. Not public.
 *
 * @author sli
 */
interface Consts {

  /**
   * Field names
   */
  interface Fields {
    String TYPE = "type";
    String ITEMS = "items";
    String ANY_OF = "anyOf";
    String PROPERTIES = "properties";
    String FORMAT = "format";
    String DOLLAR_SCHEMA = "$schema";
    String TITLE = "title";
    String DESCRIPTION = "description";
    String ADDITIONAL_PROPERTIES = "additionalProperties";
    String REQUIRED = "required";
    String EXAMPLES = "examples";
    String DEFAULT = "default";
    String MIN_ITEMS = "minItems";
    String MAX_ITEMS = "maxItems";
    String MIN_PROPERTIES = "minProperties";
    String MAX_PROPERTIES = "maxProperties";
    String MAX_LENGTH = "maxLength";
    String MIN_LENGTH = "minLength";
    String MINIMUM = "minimum";
    String MAXIMUM = "maximum";
    String MULTIPLE_OF = "multipleOf";
    String DOLLAR_COMMENT = "$comment";
    Set<String> SINGLETON_TYPE = Collections.singleton(TYPE);
  }

  /**
   * Type names
   */
  interface Types {
    String OBJECT = "object";
    String ARRAY = "array";
    String STRING = "string";
    String BOOLEAN = "boolean";
    String INTEGER = "integer";
    String NUMBER = "number";
    String NULL = "null";
    Set<String> NUMBER_TYPES = unmodifiableSetOf(NUMBER, INTEGER);
    Set<String> CONTAINER_TYPES = unmodifiableSetOf(OBJECT, ARRAY);
  }

}
