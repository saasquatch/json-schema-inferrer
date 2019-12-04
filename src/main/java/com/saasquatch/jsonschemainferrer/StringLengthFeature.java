package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.getSerializedTextLength;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Features for string length validation
 *
 * @author sli
 */
public enum StringLengthFeature implements GenericSchemaFeature {

  /**
   * {@code minLength}
   */
  MIN_LENGTH {
    @Override
    public ObjectNode getFeatureResult(GenericSchemaFeatureInput input) {
      if (!Consts.Types.STRING.equals(input.getType())) {
        return null;
      }
      final ObjectNode result = newObject();
     input.getSamples().stream()
          .mapToInt(j -> getSerializedTextLength(j))
          .min()
          .ifPresent(minLength -> result.put(Consts.Fields.MIN_LENGTH, minLength));
      return result;
    }
  },

  /**
   * {@code maxLength}
   */
  MAX_LENGTH {
    @Override
    public ObjectNode getFeatureResult(GenericSchemaFeatureInput input) {
      if (!Consts.Types.STRING.equals(input.getType())) {
        return null;
      }
      final ObjectNode result = newObject();
      input.getSamples().stream()
          .mapToInt(j -> getSerializedTextLength(j))
          .max()
          .ifPresent(maxLength -> result.put(Consts.Fields.MAX_LENGTH, maxLength));
      return result;
    }
  },;


}
