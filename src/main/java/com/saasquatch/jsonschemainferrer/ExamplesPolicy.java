package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Policy for {@code examples}. Implementations are expected to be stateless and thread safe.
 *
 * @author sli
 */
@FunctionalInterface
public interface ExamplesPolicy extends GenericSchemaFeature {

  /**
   * @return The appropriate {@code examples} {@link JsonNode} for the given input
   */
  @Nullable
  JsonNode getExamples(@Nonnull GenericSchemaFeatureInput input);

  @Override
  default ObjectNode getFeatureResult(GenericSchemaFeatureInput input) {
    if (Consts.Types.CONTAINER_TYPES.contains(input.getType())) {
      return null;
    }
    final JsonNode examples = getExamples(input);
    if (examples == null) {
      return null;
    }
    final ObjectNode result = newObject();
    result.set(Consts.Fields.EXAMPLES, examples);
    return result;
  }

}
