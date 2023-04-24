package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.stream;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.stringColToArrayDistinct;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.saasquatch.jsonschemainferrer.annotations.Beta;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for {@link AdditionalPropertiesPolicy}
 *
 * @author sli
 */
public final class AdditionalPropertiesPolicies {

  private AdditionalPropertiesPolicies() {}

  /**
   * @return A singleton {@link AdditionalPropertiesPolicy} that does nothing
   */
  public static AdditionalPropertiesPolicy noOp() {
    return input -> null;
  }

  /**
   * @return A singleton {@link AdditionalPropertiesPolicy} that always sets
   *         {@code additionalProperties} to true
   */
  public static AdditionalPropertiesPolicy allowed() {
    return input -> JsonNodeFactory.instance.booleanNode(true);
  }

  /**
   * @return A singleton {@link AdditionalPropertiesPolicy} that always sets
   *         {@code additionalProperties} to false
   */
  public static AdditionalPropertiesPolicy notAllowed() {
    return input -> JsonNodeFactory.instance.booleanNode(false);
  }

  /**
   * @return A singleton {@link AdditionalPropertiesPolicy} that sets {@code additionalProperties}
   *         to existing types on the schema
   */
  @Beta
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
      switch (existingTypes.size()) {
        case 0:
          return JsonNodeFactory.instance.booleanNode(false);
        case 1:
          return newObject().put(Consts.Fields.TYPE, existingTypes.iterator().next());
        default: {
          final ObjectNode additionalProps = newObject();
          additionalProps.set(Consts.Fields.TYPE, stringColToArrayDistinct(existingTypes));
          return additionalProps;
        }
      }
    };
  }

}
