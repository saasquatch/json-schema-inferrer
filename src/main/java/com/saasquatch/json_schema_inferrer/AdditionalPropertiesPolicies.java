package com.saasquatch.json_schema_inferrer;

import static com.saasquatch.json_schema_inferrer.JunkDrawer.newObject;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.stream;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.stringColToArrayNode;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AdditionalPropertiesPolicies {

  public static AdditionalPropertiesPolicy noOp() {
    return input -> {};
  }

  public static AdditionalPropertiesPolicy allowed() {
    return input -> {
      final ObjectNode schema = input.getSchema();
      schema.put(Consts.Fields.ADDITIONAL_PROPERTIES, true);
    };
  }

  public static AdditionalPropertiesPolicy notAllowed() {
    return input -> {
      final ObjectNode schema = input.getSchema();
      schema.put(Consts.Fields.ADDITIONAL_PROPERTIES, false);
    };
  }

  public static AdditionalPropertiesPolicy existingTypes() {
    return input -> {
      final ObjectNode schema = input.getSchema();
      final Set<String> existingTypes = stream(schema.path(Consts.Fields.PROPERTIES))
          .map(j -> j.path(Consts.Fields.TYPE)).flatMap(typeNode -> {
            if (typeNode.isTextual()) {
              return Stream.of(typeNode.textValue());
            } else if (typeNode.isArray()) {
              return stream(typeNode).map(JsonNode::textValue).filter(Objects::nonNull);
            }
            return Stream.empty();
          }).collect(Collectors.toSet());
      final ObjectNode additionalProps = newObject();
      switch (existingTypes.size()) {
        case 0:
          schema.put(Consts.Fields.ADDITIONAL_PROPERTIES, false);
          break;
        case 1:
          additionalProps.put(Consts.Fields.TYPE, existingTypes.iterator().next());
          break;
        default:
          additionalProps.set(Consts.Fields.TYPE, stringColToArrayNode(existingTypes));
          break;
      }
      schema.set(Consts.Fields.ADDITIONAL_PROPERTIES, additionalProps);
    };
  }

}
