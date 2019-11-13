package com.saasquatch.json_schema_inferrer;

import static com.saasquatch.json_schema_inferrer.JunkDrawer.getCommonFieldNames;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.stringColToArrayDistinct;
import java.util.Collection;
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
  private static JsonNode handleCommonFields(@Nonnull RequiredPolicyInput input, boolean nonNull) {
    final Collection<JsonNode> samples = input.getSamples();
    final Set<String> commonFieldNames = getCommonFieldNames(samples, nonNull);
    if (commonFieldNames.isEmpty()) {
      return null;
    }
    return stringColToArrayDistinct(commonFieldNames);
  }

}
