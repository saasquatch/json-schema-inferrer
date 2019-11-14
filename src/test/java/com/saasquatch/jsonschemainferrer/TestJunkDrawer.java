package com.saasquatch.jsonschemainferrer;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public final class TestJunkDrawer {

  public static final JsonNodeFactory jnf = JsonNodeFactory.instance;
  public static final ObjectMapper mapper = new ObjectMapper();

  public static Set<String> toStringSet(JsonNode arrayNode) {
    final Set<String> result = new HashSet<>();
    arrayNode.forEach(j -> result.add(j.textValue()));
    return result;
  }

}
