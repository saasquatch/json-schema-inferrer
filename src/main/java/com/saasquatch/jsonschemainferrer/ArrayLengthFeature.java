package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.annotation.Nonnull;

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
    public ObjectNode getFeatureResult(@Nonnull GenericSchemaFeatureInput input) {
      if (!Consts.Types.ARRAY.equals(input.getType())) {
        return null;
      }
      final ObjectNode result = newObject();
      input.getSamples().stream()
          .filter(JsonNode::isArray)
          .mapToInt(JsonNode::size)
          .min()
          .ifPresent(minItems -> result.put(Consts.Fields.MIN_ITEMS, minItems));
      return result;
    }
  },

  /**
   * {@code maxItems}
   */
  MAX_ITEMS {
    @Override
    public ObjectNode getFeatureResult(@Nonnull GenericSchemaFeatureInput input) {
      if (!Consts.Types.ARRAY.equals(input.getType())) {
        return null;
      }
      final ObjectNode result = newObject();
      input.getSamples().stream()
          .filter(JsonNode::isArray)
          .mapToInt(JsonNode::size)
          .max()
          .ifPresent(maxItems -> result.put(Consts.Fields.MAX_ITEMS, maxItems));
      return result;
    }
  },
  ;

}
