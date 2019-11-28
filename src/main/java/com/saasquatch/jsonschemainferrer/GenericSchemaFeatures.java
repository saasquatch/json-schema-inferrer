package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import java.util.Objects;
import javax.annotation.Nonnull;
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
