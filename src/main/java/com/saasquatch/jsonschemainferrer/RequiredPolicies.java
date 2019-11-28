package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.getCommonFieldNames;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.stringColToArrayDistinct;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utilities for {@link RequiredPolicy}
 *
 * @author sli
 */
public final class RequiredPolicies {

  private RequiredPolicies() {}

  /**
   * @return A singleton {@link RequiredPolicy} that does nothing
   */
  public static RequiredPolicy noOp() {
    return input -> null;
  }

  /**
   * @return A singleton {@link RequiredPolicy} that sets {@code required} to field names common to
   *         the given samples.
   */
  public static RequiredPolicy commonFields() {
    return input -> handleCommonFields(input, false);
  }

  /**
   * @return A singleton {@link RequiredPolicy} that sets {@code required} to field names common to
   *         the given samples that are not null.
   */
  public static RequiredPolicy nonNullCommonFields() {
    return input -> handleCommonFields(input, true);
  }

  @Nullable
  private static JsonNode handleCommonFields(@Nonnull GenericSchemaAddOnInput input,
      boolean nonNull) {
    final Set<String> commonFieldNames = getCommonFieldNames(input.getSamples(), nonNull);
    if (commonFieldNames.isEmpty()) {
      return null;
    }
    return stringColToArrayDistinct(commonFieldNames);
  }

}
