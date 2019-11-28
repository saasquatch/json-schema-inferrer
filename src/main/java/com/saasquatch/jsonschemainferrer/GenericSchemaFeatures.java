package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import java.util.Objects;
import java.util.OptionalInt;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utilities for {@link GenericSchemaFeature}
 *
 * @author sli
 */
public final class GenericSchemaFeatures {

  private GenericSchemaFeatures() {}

  /**
   * @return A singleton {@link GenericSchemaFeature} that does nothing.
   */
  public static GenericSchemaFeature noOp() {
    return input -> null;
  }

  /**
   * @return A singleton {@link GenericSchemaFeature} that infers {@code minProperties}.
   */
  public static GenericSchemaFeature minProperties() {
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
   * @return A singleton {@link GenericSchemaFeature} that infers {@code maxProperties}.
   */
  public static GenericSchemaFeature maxProperties() {
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
   * @return An {@link GenericSchemaFeature} that uses the given {@link GenericSchemaFeature}s in the
   *         given order, overwriting previous results if add-ons with the same field names exist.
   * @throws NullPointerException if the input has null elements
   */
  public static GenericSchemaFeature chained(@Nonnull GenericSchemaFeature... addOns) {
    for (GenericSchemaFeature addOn : addOns) {
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

  private static GenericSchemaFeature _chained(@Nonnull GenericSchemaFeature[] addOns) {
    return input -> {
      final ObjectNode result = newObject();
      for (GenericSchemaFeature addOn : addOns) {
        final ObjectNode addOnObj = addOn.getFeatureResult(input);
        if (addOnObj != null) {
          result.setAll(addOnObj);
        }
      }
      return result.isEmpty() ? null : result;
    };
  }

}
