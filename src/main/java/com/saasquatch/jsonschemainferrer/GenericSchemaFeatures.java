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
   * @return An {@link GenericSchemaFeature} that uses the given {@link GenericSchemaFeature}s in
   *         the given order, overwriting previous results if add-ons with the same field names
   *         exist.
   * @throws NullPointerException if the input has null elements
   */
  public static GenericSchemaFeature chained(@Nonnull GenericSchemaFeature... features) {
    for (GenericSchemaFeature feature : features) {
      Objects.requireNonNull(feature);
    }
    switch (features.length) {
      case 0:
        return noOp();
      case 1:
        return features[0];
      default:
        break;
    }
    return input -> {
      final ObjectNode result = newObject();
      for (GenericSchemaFeature feature : features) {
        final ObjectNode featureResult = feature.getFeatureResult(input);
        if (featureResult != null) {
          result.setAll(featureResult);
        }
      }
      return result.isEmpty() ? null : result;
    };
  }

}
