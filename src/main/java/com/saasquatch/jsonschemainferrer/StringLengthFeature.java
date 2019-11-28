package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.getSerializedTextLength;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import java.util.OptionalInt;
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
      final OptionalInt optMinLength = input.getSamples().stream()
          .mapToInt(j -> getSerializedTextLength(j)).filter(len -> len >= 0).min();
      if (!optMinLength.isPresent()) {
        return null;
      }
      final ObjectNode result = newObject();
      result.put(Consts.Fields.MIN_LENGTH, optMinLength.getAsInt());
      return result;
    }
  },
  /**
   * {@code maxLength}
   */
  MAX_LENGTH {
    @Override
    public ObjectNode getFeatureResult(GenericSchemaFeatureInput input) {
      final OptionalInt optMaxLength = input.getSamples().stream()
          .mapToInt(j -> getSerializedTextLength(j)).filter(len -> len >= 0).max();
      if (!optMaxLength.isPresent()) {
        return null;
      }
      final ObjectNode result = newObject();
      result.put(Consts.Fields.MAX_LENGTH, optMaxLength.getAsInt());
      return result;
    }
  },;


}
