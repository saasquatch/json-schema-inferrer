package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.annotation.Nonnull;

/**
 * Features for object size validation
 *
 * @author sli
 */
public enum ObjectSizeFeature implements GenericSchemaFeature {

  /**
   * {@code minProperties}
   */
  MIN_PROPERTIES {
    @Override
    public ObjectNode getFeatureResult(@Nonnull GenericSchemaFeatureInput input) {
      if (!Consts.Types.OBJECT.equals(input.getType())) {
        return null;
      }
      final ObjectNode result = newObject();
      input.getSamples().stream()
          .filter(JsonNode::isObject)
          .mapToInt(JsonNode::size)
          .min()
          .ifPresent(minProps -> result.put(Consts.Fields.MIN_PROPERTIES, minProps));
      return result;
    }
  },

  /**
   * {@code maxProperties}
   */
  MAX_PROPERTIES {
    @Override
    public ObjectNode getFeatureResult(@Nonnull GenericSchemaFeatureInput input) {
      if (!Consts.Types.OBJECT.equals(input.getType())) {
        return null;
      }
      final ObjectNode result = newObject();
      input.getSamples().stream()
          .filter(JsonNode::isObject)
          .mapToInt(JsonNode::size)
          .max()
          .ifPresent(maxProps -> result.put(Consts.Fields.MAX_PROPERTIES, maxProps));
      return result;
    }
  },
  ;

}
