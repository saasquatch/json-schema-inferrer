package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.isValidEnum;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.isValidEnumIgnoreCase;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utilities for {@link PrimitiveEnumCriterion}
 *
 * @author sli
 */
public final class PrimitiveEnumCriteria {

  private PrimitiveEnumCriteria() {}

  /**
   * @return A singleton {@link PrimitiveEnumCriterion} that always returns false
   */
  public static PrimitiveEnumCriterion noOp() {
    return input -> false;
  }

  /**
   * @return An {@link PrimitiveEnumCriterion} that returns true if all the samples are valid enum
   *         values of a Java enum.
   */
  public static <E extends Enum<E>> PrimitiveEnumCriterion validEnum(@Nonnull Class<E> enumClass) {
    Objects.requireNonNull(enumClass);
    return input -> {
      return input.getSamples().stream()
          .map(JsonNode::textValue)
          .allMatch(textValue -> isValidEnum(enumClass, textValue));
    };
  }

  /**
   * @return An {@link PrimitiveEnumCriterion} that returns true if all the samples are valid enum
   *         values of a Java enum ignoring case.
   */
  public static <E extends Enum<E>> PrimitiveEnumCriterion validEnumIgnoreCase(
      @Nonnull Class<E> enumClass) {
    Objects.requireNonNull(enumClass);
    return input -> {
      return input.getSamples().stream()
          .map(JsonNode::textValue)
          .allMatch(textValue -> isValidEnumIgnoreCase(enumClass, textValue));
    };
  }

  /**
   * Convenience method for {@link #or(List)}.
   */
  public static PrimitiveEnumCriterion or(@Nonnull PrimitiveEnumCriterion... criteria) {
    return or(Arrays.asList(criteria));
  }

  /**
   * @return An {@link PrimitiveEnumCriterion} that is a logical or of the given criteria
   * @throws NullPointerException if the input has null elements
   * @throws IllegalArgumentException if the input is empty
   */
  public static PrimitiveEnumCriterion or(@Nonnull List<PrimitiveEnumCriterion> criteria) {
    for (PrimitiveEnumCriterion criterion : criteria) {
      Objects.requireNonNull(criterion);
    }
    switch (criteria.size()) {
      case 0:
        throw new IllegalArgumentException("Empty criteria");
      case 1:
        return criteria.get(0);
      default:
        break;
    }
    // Defensive copy
    final PrimitiveEnumCriterion[] criteriaArray = criteria.toArray(new PrimitiveEnumCriterion[0]);
    return input -> {
      for (PrimitiveEnumCriterion criterion : criteriaArray) {
        if (criterion.isEnum(input)) {
          return true;
        }
      }
      return false;
    };
  }

}
