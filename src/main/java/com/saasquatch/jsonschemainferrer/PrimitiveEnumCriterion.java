package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;

/**
 * Determine whether the given primitive samples should be an {@code enum}.
 *
 * @author sli
 * @see PrimitiveEnumCriteria
 */
@FunctionalInterface
public interface PrimitiveEnumCriterion {

  /**
   * @return Whether the given primitive samples are an {@code enum}
   */
  boolean isEnum(@Nonnull PrimitiveEnumCriterionInput input);

}
