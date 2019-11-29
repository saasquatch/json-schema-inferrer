package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import java.util.Arrays;
import java.util.List;
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
   * Convenience method for {@link #chained(List)}
   */
  public static GenericSchemaFeature chained(@Nonnull GenericSchemaFeature... features) {
    return chained(Arrays.asList(features));
  }

  /**
   * @return An {@link GenericSchemaFeature} that uses the given {@link GenericSchemaFeature}s in
   *         the given order, overwriting previous results if add-ons with the same field names
   *         exist.
   * @throws NullPointerException if the input has null elements
   */
  public static GenericSchemaFeature chained(@Nonnull List<GenericSchemaFeature> features) {
    features.forEach(Objects::requireNonNull);
    switch (features.size()) {
      case 0:
        return noOp();
      case 1:
        return features.get(0);
      default:
        break;
    }
    // Defensive copy
    final GenericSchemaFeature[] featuresArray = features.toArray(new GenericSchemaFeature[0]);
    return input -> {
      final ObjectNode result = newObject();
      for (GenericSchemaFeature feature : featuresArray) {
        final ObjectNode featureResult = feature.getFeatureResult(input);
        if (featureResult != null) {
          result.setAll(featureResult);
        }
      }
      return result.isEmpty() ? null : result;
    };
  }

}
