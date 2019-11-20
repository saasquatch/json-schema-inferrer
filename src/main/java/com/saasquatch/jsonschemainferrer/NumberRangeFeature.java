package com.saasquatch.jsonschemainferrer;

import java.util.Comparator;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Features for number range validation
 *
 * @author sli
 */
public enum NumberRangeFeature {

  /**
   * {@code minimum}
   */
  MINIMUM {
    @Override
    void process(ObjectNode schema, PrimitivesSummary primitivesSummary,
        JsonSchemaInferrer jsonSchemaInferrer) {
      primitivesSummary.getSamples().stream().filter(JsonNode::isNumber).min(NUM_VALUE_COMPARATOR)
          .ifPresent(minNode -> schema.set(Consts.Fields.MINIMUM, minNode));
    }
  },
  /**
   * {@code maximum}
   */
  MAXIMUM {
    @Override
    void process(ObjectNode schema, PrimitivesSummary primitivesSummary,
        JsonSchemaInferrer jsonSchemaInferrer) {
      primitivesSummary.getSamples().stream().filter(JsonNode::isNumber).max(NUM_VALUE_COMPARATOR)
          .ifPresent(maxNode -> schema.set(Consts.Fields.MAXIMUM, maxNode));
    }
  },;

  private static final Comparator<JsonNode> NUM_VALUE_COMPARATOR =
      Comparator.comparing(JsonNode::decimalValue);

  abstract void process(@Nonnull ObjectNode schema, @Nonnull PrimitivesSummary primitivesSummary,
      @Nonnull JsonSchemaInferrer jsonSchemaInferrer);

}
