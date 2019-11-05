package com.saasquatch.json_schema_inferrer;

import static com.saasquatch.json_schema_inferrer.JunkDrawer.stream;
import java.util.Set;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utilities for {@link RequiredPolicy}
 *
 * @author sli
 */
public final class RequiredPolicies {

  /**
   * @return A singleton {@link RequiredPolicy} that does nothing
   */
  public static RequiredPolicy noOp() {
    return input -> null;
  }

  /**
   * @return A singleton {@link RequiredPolicy} that sets {@code required} to all the existing field
   *         names
   */
  public static RequiredPolicy existingFieldNames() {
    return input -> {
      final ObjectNode schema = input.getSchema();
      final Set<String> fieldNames = stream(schema.path(Consts.Fields.PROPERTIES).fieldNames())
          .collect(Collectors.toSet());
      if (fieldNames.isEmpty()) {
        return null;
      }
      return JunkDrawer.stringColToArrayDistinct(fieldNames);
    };
  }

}
