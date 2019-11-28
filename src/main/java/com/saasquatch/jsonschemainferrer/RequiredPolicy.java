package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Policy for {@code required}. Implementations are expected to be stateless and thread safe.
 *
 * @author sli
 * @see RequiredPolicies
 */
@FunctionalInterface
public interface RequiredPolicy extends GenericSchemaFeature {

  /**
   * Get the appropriate {@code required} field for the input. Note that this method should not
   * modify the input.
   */
  JsonNode getRequired(@Nonnull GenericSchemaAddOnInput input);

  @Override
  default ObjectNode getResult(GenericSchemaAddOnInput input) {
    if (!Consts.Types.OBJECT.equals(input.getType())) {
      return null;
    }
    final JsonNode required = getRequired(input);
    if (required == null) {
      return null;
    }
    final ObjectNode result = newObject();
    result.set(Consts.Fields.REQUIRED, required);
    return result;
  }

}
