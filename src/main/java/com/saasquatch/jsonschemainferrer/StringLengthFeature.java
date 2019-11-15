package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Features for string length validation
 *
 * @author sli
 */
public enum StringLengthFeature {

  /**
   * {@code minLength}
   */
  MIN_LENGTH {
    @Override
    void process(ObjectNode schema, PrimitivesSummary primitivesSummary,
        JsonSchemaInferrer jsonSchemaInferrer) {
      primitivesSummary.getMinStringLength()
          .ifPresent(minLength -> schema.put(Consts.Fields.MIN_LENGTH, minLength));
    }
  },
  /**
   * {@code maxLength}
   */
  MAX_LENGTH {
    @Override
    void process(ObjectNode schema, PrimitivesSummary primitivesSummary,
        JsonSchemaInferrer jsonSchemaInferrer) {
      primitivesSummary.getMaxStringLength()
          .ifPresent(maxLength -> schema.put(Consts.Fields.MAX_LENGTH, maxLength));
    }
  },;

  abstract void process(@Nonnull ObjectNode schema, @Nonnull PrimitivesSummary primitivesSummary,
      @Nonnull JsonSchemaInferrer jsonSchemaInferrer);

}
