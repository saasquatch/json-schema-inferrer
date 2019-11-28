package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import java.util.OptionalInt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Features for array length validation
 *
 * @author sli
 */
public enum ArrayLengthFeature implements GenericSchemaFeature {

  /**
   * {@code minItems}
   */
  MIN_ITEMS {
    @Override
    public ObjectNode getResult(GenericSchemaAddOnInput input) {
      final OptionalInt optMinItems =
          input.getSamples().stream().filter(JsonNode::isArray).mapToInt(JsonNode::size).min();
      if (!optMinItems.isPresent()) {
        return null;
      }
      final ObjectNode result = newObject();
      result.put(Consts.Fields.MIN_ITEMS, optMinItems.getAsInt());
      return result;
    }
  },
  /**
   * {@code maxItems}
   */
  MAX_ITEMS {
    @Override
    public ObjectNode getResult(GenericSchemaAddOnInput input) {
      final OptionalInt optMaxItems =
          input.getSamples().stream().filter(JsonNode::isArray).mapToInt(JsonNode::size).max();
      if (!optMaxItems.isPresent()) {
        return null;
      }
      final ObjectNode result = newObject();
      result.put(Consts.Fields.MAX_ITEMS, optMaxItems.getAsInt());
      return result;
    }
  },;

}
