package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Features for array length validation
 *
 * @author sli
 */
public enum ArrayLengthFeature {

  /**
   * {@code minItems}
   */
  MIN_ITEMS {
    @Override
    void process(ObjectNode schema, Collection<ArrayNode> samples,
        JsonSchemaInferrer jsonSchemaInferrer) {
      samples.stream().mapToInt(JsonNode::size).min()
          .ifPresent(minItems -> schema.put(Consts.Fields.MIN_ITEMS, minItems));
    }
  },
  /**
   * {@code maxItems}
   */
  MAX_ITEMS {
    @Override
    void process(ObjectNode schema, Collection<ArrayNode> samples,
        JsonSchemaInferrer jsonSchemaInferrer) {
      samples.stream().mapToInt(JsonNode::size).max()
          .ifPresent(maxItems -> schema.put(Consts.Fields.MAX_ITEMS, maxItems));
    }
  },;

  abstract void process(@Nonnull ObjectNode schema, @Nonnull Collection<ArrayNode> samples,
      @Nonnull JsonSchemaInferrer jsonSchemaInferrer);

}
