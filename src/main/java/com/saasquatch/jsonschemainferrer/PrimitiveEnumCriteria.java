package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.isValidEnum;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.isValidEnumIgnoreCase;
import java.util.Objects;
import javax.annotation.Nonnull;

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
          .allMatch(j -> isValidEnum(enumClass, j.textValue()));
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
          .allMatch(j -> isValidEnumIgnoreCase(enumClass, j.textValue()));
    };
  }

  /**
   * @return An {@link PrimitiveEnumCriterion} that is a logical or of the given criteria
   * @throws NullPointerException if the input has null elements
   * @throws IllegalArgumentException if the input is empty
   */
  public static PrimitiveEnumCriterion or(@Nonnull PrimitiveEnumCriterion... criteria) {
    for (PrimitiveEnumCriterion criterion : criteria) {
      Objects.requireNonNull(criterion);
    }
    switch (criteria.length) {
      case 0:
        throw new IllegalArgumentException("Empty criteria");
      case 1:
        return criteria[0];
      default:
        break;
    }
    // Defensive copy
    return input -> {
      for (PrimitiveEnumCriterion criterion : criteria) {
        if (criterion.isEnum(input)) {
          return true;
        }
      }
      return false;
    };
  }

}
