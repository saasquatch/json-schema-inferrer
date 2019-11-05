package com.saasquatch.json_schema_inferrer;

import static com.saasquatch.json_schema_inferrer.JunkDrawer.newObject;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.stream;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.stringColToArrayDistinct;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utilities for {@link AdditionalPropertiesPolicy}
 *
 * @author sli
 */
public class AdditionalPropertiesPolicies {

  /**
   * @return A singleton {@link AdditionalPropertiesPolicy} that does nothing
   */
  public static AdditionalPropertiesPolicy noOp() {
    return input -> {};
  }

  /**
   * @return A singleton {@link AdditionalPropertiesPolicy} that always sets
   *         {@code additionalProperties} to true
   */
  public static AdditionalPropertiesPolicy allowed() {
    return input -> {
      final ObjectNode schema = input.getSchema();
      schema.put(Consts.Fields.ADDITIONAL_PROPERTIES, true);
    };
  }

  /**
   * @return A singleton {@link AdditionalPropertiesPolicy} that always sets
   *         {@code additionalProperties} to false
   */
  public static AdditionalPropertiesPolicy notAllowed() {
    return input -> {
      final ObjectNode schema = input.getSchema();
      schema.put(Consts.Fields.ADDITIONAL_PROPERTIES, false);
    };
  }

  /**
   * @return A singleton {@link AdditionalPropertiesPolicy} that sets {@code additionalProperties}
   *         to existing types on the schema
   */
  public static AdditionalPropertiesPolicy existingTypes() {
    return input -> {
      final ObjectNode schema = input.getSchema();
      final Set<String> existingTypes = stream(schema.path(Consts.Fields.PROPERTIES))
          .map(j -> j.path(Consts.Fields.TYPE))
          .flatMap(typeNode -> {
            if (typeNode.isTextual()) {
              return Stream.of(typeNode.textValue());
            } else if (typeNode.isArray()) {
              return stream(typeNode)
                  .map(JsonNode::textValue)
                  .filter(Objects::nonNull);
            }
            return Stream.empty();
          })
          .collect(Collectors.toSet());
      final ObjectNode additionalProps = newObject();
      switch (existingTypes.size()) {
        case 0:
          schema.put(Consts.Fields.ADDITIONAL_PROPERTIES, false);
          break;
        case 1:
          additionalProps.put(Consts.Fields.TYPE, existingTypes.iterator().next());
          break;
        default:
          additionalProps.set(Consts.Fields.TYPE, stringColToArrayDistinct(existingTypes));
          break;
      }
      schema.set(Consts.Fields.ADDITIONAL_PROPERTIES, additionalProps);
    };
  }

}
