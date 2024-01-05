package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Comparator;
import javax.annotation.Nonnull;

/**
 * Features for number range validation
 *
 * @author sli
 */
public enum NumberRangeFeature implements GenericSchemaFeature {

  /**
   * {@code minimum}
   */
  MINIMUM {
    @Override
    public ObjectNode getFeatureResult(@Nonnull GenericSchemaFeatureInput input) {
      if (!Consts.Types.NUMBER_TYPES.contains(input.getType())) {
        return null;
      }
      return input.getSamples().stream()
          .filter(JsonNode::isNumber)
          .min(NUM_VALUE_COMPARATOR)
          .map(minNode -> {
            final ObjectNode result = newObject();
            result.set(Consts.Fields.MINIMUM, minNode);
            return result;
          })
          .orElse(null);
    }
  },

  /**
   * {@code maximum}
   */
  MAXIMUM {
    @Override
    public ObjectNode getFeatureResult(@Nonnull GenericSchemaFeatureInput input) {
      if (!Consts.Types.NUMBER_TYPES.contains(input.getType())) {
        return null;
      }
      return input.getSamples().stream()
          .filter(JsonNode::isNumber)
          .max(NUM_VALUE_COMPARATOR)
          .map(maxNode -> {
            final ObjectNode result = newObject();
            result.set(Consts.Fields.MAXIMUM, maxNode);
            return result;
          })
          .orElse(null);
    }
  },
  ;

  private static final Comparator<JsonNode> NUM_VALUE_COMPARATOR =
      Comparator.comparing(JsonNode::decimalValue);

}
