package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import java.util.Objects;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utilities for {@link GenericSchemaValidationFeature}
 *
 * @author sli
 */
public final class GenericSchemaValidationFeatures {

  private GenericSchemaValidationFeatures() {}

  /**
   * @return A singleton {@link GenericSchemaValidationFeature} that does nothing.
   */
  public static GenericSchemaValidationFeature noOp() {
    return input -> null;
  }

  /**
   * @return An {@link GenericSchemaValidationFeature} that uses the given
   *         {@link GenericSchemaValidationFeature}s in the given order, overwriting previous
   *         results if add-ons with the same field names exist.
   * @throws NullPointerException if the input has null elements
   */
  public static GenericSchemaValidationFeature chained(
      @Nonnull GenericSchemaValidationFeature... features) {
    for (GenericSchemaValidationFeature feature : features) {
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
    return _chained(features.clone());
  }

  private static GenericSchemaValidationFeature _chained(
      @Nonnull GenericSchemaValidationFeature[] features) {
    return input -> {
      final ObjectNode result = newObject();
      for (GenericSchemaValidationFeature feature : features) {
        final ObjectNode featureResult = feature.getFeatureResult(input);
        if (featureResult != null) {
          result.setAll(featureResult);
        }
      }
      return result.isEmpty() ? null : result;
    };
  }

}
