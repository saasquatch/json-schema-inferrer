package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import java.util.Comparator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Features for number range validation
 *
 * @author sli
 */
public enum NumberRangeFeature implements GenericSchemaAddOn {

  /**
   * {@code minimum}
   */
  MINIMUM {
    @Override
    public ObjectNode getAddOn(GenericSchemaAddOnInput input) {
      return input.getSamples().stream().filter(JsonNode::isNumber).min(NUM_VALUE_COMPARATOR)
          .map(minNode -> {
            final ObjectNode result = newObject();
            result.set(Consts.Fields.MINIMUM, minNode);
            return result;
          }).orElse(null);
    }
  },
  /**
   * {@code maximum}
   */
  MAXIMUM {
    @Override
    public ObjectNode getAddOn(GenericSchemaAddOnInput input) {
      return input.getSamples().stream().filter(JsonNode::isNumber).max(NUM_VALUE_COMPARATOR)
          .map(maxNode -> {
            final ObjectNode result = newObject();
            result.set(Consts.Fields.MAXIMUM, maxNode);
            return result;
          }).orElse(null);
    }
  },;

  private static final Comparator<JsonNode> NUM_VALUE_COMPARATOR =
      Comparator.comparing(JsonNode::decimalValue);

}
