package com.saasquatch.jsonschemainferrer;

import java.util.Collections;
import java.util.Set;

/**
 * Constants. Not public.
 *
 * @author sli
 */
interface Consts {

  interface Fields {
    String TYPE = "type", ITEMS = "items", ANY_OF = "anyOf", PROPERTIES = "properties",
        FORMAT = "format", DOLLAR_SCHEMA = "$schema", TITLE = "title", DESCRIPTION = "description",
        ADDITIONAL_PROPERTIES = "additionalProperties", REQUIRED = "required",
        EXAMPLES = "examples", DEFAULT = "default", MIN_ITEMS = "minItems", MAX_ITEMS = "maxItems",
        MIN_PROPERTIES = "minProperties", MAX_PROPERTIES = "maxProperties",
        MAX_LENGTH = "maxLength", MIN_LENGTH = "minLength";
    Set<String> SINGLETON_TYPE = Collections.singleton(TYPE);
  }

  interface Types {
    String OBJECT = "object", ARRAY = "array", STRING = "string", BOOLEAN = "boolean",
        INTEGER = "integer", NUMBER = "number", NULL = "null";
  }

}
