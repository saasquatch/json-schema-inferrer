package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import java.util.Objects;
import java.util.OptionalInt;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utilities for {@link ObjectSchemaAddOn}
 *
 * @author sli
 */
public final class ObjectSchemaAddOns {

  private ObjectSchemaAddOns() {}

  /**
   * @return A singleton {@link ObjectSchemaAddOn} that does nothing.
   */
  public static ObjectSchemaAddOn noOp() {
    return input -> null;
  }

  /**
   * @return A singleton {@link ObjectSchemaAddOn} that infers {@code minProperties}.
   */
  public static ObjectSchemaAddOn minProperties() {
    return input -> {
      final OptionalInt optMinProps = input.getSamples().stream().mapToInt(JsonNode::size).min();
      if (!optMinProps.isPresent()) {
        return null;
      }
      final int minProps = optMinProps.getAsInt();
      return newObject().put(Consts.Fields.MIN_PROPERTIES, minProps);
    };
  }

  /**
   * @return A singleton {@link ObjectSchemaAddOn} that infers {@code maxProperties}.
   */
  public static ObjectSchemaAddOn maxProperties() {
    return input -> {
      final OptionalInt optMaxProps = input.getSamples().stream().mapToInt(JsonNode::size).max();
      if (!optMaxProps.isPresent()) {
        return null;
      }
      final int maxProps = optMaxProps.getAsInt();
      return newObject().put(Consts.Fields.MAX_PROPERTIES, maxProps);
    };
  }

  /**
   * @return An {@link ObjectSchemaAddOn} that uses the given {@link ObjectSchemaAddOn}s in the
   *         given order, overwriting previous results if add-ons with the same field names exist.
   * @throws NullPointerException if the input has null elements
   */
  public static ObjectSchemaAddOn chained(@Nonnull ObjectSchemaAddOn... addOns) {
    for (ObjectSchemaAddOn addOn : addOns) {
      Objects.requireNonNull(addOn);
    }
    switch (addOns.length) {
      case 0:
        return noOp();
      case 1:
        return addOns[0];
      default:
        break;
    }
    return _chained(addOns.clone());
  }

  private static ObjectSchemaAddOn _chained(@Nonnull ObjectSchemaAddOn[] addOns) {
    return input -> {
      final ObjectNode result = newObject();
      for (ObjectSchemaAddOn addOn : addOns) {
        final ObjectNode addOnObj = addOn.getAddOn(input);
        if (addOnObj != null) {
          result.setAll(addOnObj);
        }
      }
      return result.isEmpty() ? null : result;
    };
  }

}
