package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Policy for {@code default}
 *
 * @author sli
 */
@FunctionalInterface
public interface DefaultPolicy extends GenericSchemaFeature {

  /**
   * Get the appropriate {@code default} from the given input.
   */
  @Nullable
  JsonNode getDefault(@Nonnull GenericSchemaFeatureInput input);

  @Override
  default ObjectNode getFeatureResult(GenericSchemaFeatureInput input) {
    if (Consts.Types.CONTAINER_TYPES.contains(input.getType())) {
      return null;
    }
    final JsonNode defaultNode = getDefault(input);
    if (defaultNode == null) {
      return null;
    }
    final ObjectNode result = newObject();
    result.set(Consts.Fields.DEFAULT, defaultNode);
    return result;
  }

}
