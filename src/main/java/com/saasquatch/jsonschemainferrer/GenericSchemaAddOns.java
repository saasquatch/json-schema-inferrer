package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import java.util.Objects;
import java.util.OptionalInt;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utilities for {@link GenericSchemaAddOn}
 *
 * @author sli
 */
public final class GenericSchemaAddOns {

  private GenericSchemaAddOns() {}

  /**
   * @return A singleton {@link GenericSchemaAddOn} that does nothing.
   */
  public static GenericSchemaAddOn noOp() {
    return input -> null;
  }

  /**
   * @return A singleton {@link GenericSchemaAddOn} that infers {@code minProperties}.
   */
  public static GenericSchemaAddOn minProperties() {
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
   * @return A singleton {@link GenericSchemaAddOn} that infers {@code maxProperties}.
   */
  public static GenericSchemaAddOn maxProperties() {
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
   * @return An {@link GenericSchemaAddOn} that uses the given {@link GenericSchemaAddOn}s in the
   *         given order, overwriting previous results if add-ons with the same field names exist.
   * @throws NullPointerException if the input has null elements
   */
  public static GenericSchemaAddOn chained(@Nonnull GenericSchemaAddOn... addOns) {
    for (GenericSchemaAddOn addOn : addOns) {
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

  private static GenericSchemaAddOn _chained(@Nonnull GenericSchemaAddOn[] addOns) {
    return input -> {
      final ObjectNode result = newObject();
      for (GenericSchemaAddOn addOn : addOns) {
        final ObjectNode addOnObj = addOn.getAddOn(input);
        if (addOnObj != null) {
          result.setAll(addOnObj);
        }
      }
      return result.isEmpty() ? null : result;
    };
  }

}
