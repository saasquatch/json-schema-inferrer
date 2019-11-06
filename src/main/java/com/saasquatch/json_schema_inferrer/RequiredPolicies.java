package com.saasquatch.json_schema_inferrer;

import static com.saasquatch.json_schema_inferrer.JunkDrawer.stream;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.stringColToArrayDistinct;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

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
   * @return A singleton {@link RequiredPolicy} that sets {@code required} to field names common to
   *         the given samples.
   */
  public static RequiredPolicy commonFieldNames() {
    return input -> {
      final Collection<JsonNode> samples = input.getSamples();
      final Set<String> commonFieldNames = getCommonFieldNames(samples, false);
      return stringColToArrayDistinct(commonFieldNames);
    };
  }

  /**
   * @return A singleton {@link RequiredPolicy} that sets {@code required} to field names common to
   *         the given samples that are not null.
   */
  public static RequiredPolicy nonNullCommonFieldNames() {
    return input -> {
      final Collection<JsonNode> samples = input.getSamples();
      final Set<String> commonFieldNames = getCommonFieldNames(samples, true);
      return stringColToArrayDistinct(commonFieldNames);
    };
  }

  private static Set<String> getCommonFieldNames(@Nonnull Collection<? extends JsonNode> samples,
      boolean nonNull) {
    Set<String> commonFieldNames = null;
    for (JsonNode sample : samples) {
      final Set<String> fieldNames = stream(sample.fieldNames())
          .filter(nonNull ? fieldName -> !sample.path(fieldName).isNull() : fieldName -> true)
          .collect(Collectors.toSet());
      if (commonFieldNames == null) {
        commonFieldNames = new HashSet<>(fieldNames);
      } else {
        commonFieldNames.retainAll(fieldNames);
      }
    }
    return Collections.unmodifiableSet(commonFieldNames);
  }

}
