package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import java.util.OptionalInt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Features for object size validation
 *
 * @author sli
 */
public enum ObjectSizeFeature implements GenericSchemaAddOn {

  /**
   * {@code minProperties}
   */
  MIN_PROPERTIES {
    @Override
    public ObjectNode getAddOn(GenericSchemaAddOnInput input) {
      final OptionalInt optMinProps =
          input.getSamples().stream().filter(JsonNode::isObject).mapToInt(JsonNode::size).min();
      if (!optMinProps.isPresent()) {
        return null;
      }
      final ObjectNode result = newObject();
      result.put(Consts.Fields.MIN_PROPERTIES, optMinProps.getAsInt());
      return result;
    }
  },
  /**
   * {@code maxProperties}
   */
  MAX_PROPERTIES {
    @Override
    public ObjectNode getAddOn(GenericSchemaAddOnInput input) {
      final OptionalInt optMaxProps =
          input.getSamples().stream().filter(JsonNode::isObject).mapToInt(JsonNode::size).max();
      if (!optMaxProps.isPresent()) {
        return null;
      }
      final ObjectNode result = newObject();
      result.put(Consts.Fields.MAX_PROPERTIES, optMaxProps.getAsInt());
      return result;
    }
  },;

}
