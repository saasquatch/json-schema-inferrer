package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Policy for {@code multipleOf}.
 *
 * @author sli
 * @see MultipleOfPolicies
 */
@FunctionalInterface
public interface MultipleOfPolicy extends GenericSchemaFeature {

  /**
   * @return the appropriate {@code multipleOf} {@link JsonNode} for the given input
   */
  @Nullable
  JsonNode getMultipleOf(@Nonnull GenericSchemaFeatureInput input);

  @Override
  default ObjectNode getFeatureResult(GenericSchemaFeatureInput input) {
    final JsonNode multipleOf = getMultipleOf(input);
    if (multipleOf == null) {
      return null;
    }
    final ObjectNode result = newObject();
    result.set(Consts.Fields.MULTIPLE_OF, multipleOf);
    return result;
  }

}
